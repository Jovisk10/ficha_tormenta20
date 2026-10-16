package com.jovis.t20.ui.dice

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.jovis.t20.R
import com.jovis.t20.rules.DiceModels
import com.jovis.t20.rules.Mat3
import com.jovis.t20.rules.Polyhedron
import com.jovis.t20.rules.Vec3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/*
 * Bandeja de feltro com os dados em 3D (proposta B dos esboços).
 * Cada dado é um sólido de verdade (d4, d6, d8, d10, d12 ou d20), projetado e iluminado a cada
 * quadro: entra pela lateral, quica com sombra no chão e para com o resultado de frente.
 * Rolagens com vários dados (ex.: 2d6+2) lançam todos juntos. Sem biblioteca de 3D.
 */

private const val ROLL_MILLIS = 1800
private const val FLASH_MILLIS = 650
private const val MAX_SHOWN = 6

/** Um dado na bandeja: quantos lados e em que valor ele para. */
data class TrayDie(val sides: Int, val value: Int)

/** Parâmetros sorteados para cada dado, para nenhum girar igual ao outro. */
private class Tumble(seed: Long) {
    private val r = Random(seed)
    private fun axis() = Vec3(r.nextDouble(-1.0, 1.0), r.nextDouble(-1.0, 1.0), r.nextDouble(-1.0, 1.0)).let {
        if (it.length < 0.1) Vec3(0.3, 1.0, 0.2) else it
    }
    val axis1 = axis()
    val axis2 = axis()
    val turns1 = PI * 2 * (5 + r.nextDouble())
    val turns2 = PI * (1 + r.nextDouble())
    val tilt = (r.nextDouble() - 0.5) * 0.7
    val spin2d = 360f * (3 + r.nextFloat())
    val bounce = 0.85f + r.nextFloat() * 0.3f
}

/**
 * @param rollKey identificador da rolagem (muda a cada rolagem nova)
 * @param dice os dados lançados; acima de [MAX_SHOWN], os demais aparecem como "+N"
 * @param natural20 dispara o brilho rápido ao parar
 * @param natural1 deixa o dado apagado ao parar
 * @param animate falso para mostrar os dados já parados (ao voltar para a aba, por exemplo)
 */
@Composable
fun DiceTray(
    rollKey: Long,
    dice: List<TrayDie>,
    natural20: Boolean,
    natural1: Boolean,
    animate: Boolean,
    onSettled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(1f) }
    val flash = remember { Animatable(0f) }
    val tumbles = remember(rollKey, dice.size) { List(dice.size.coerceAtMost(MAX_SHOWN)) { Tumble(rollKey * 7919 + it * 104729 + 13) } }
    val context = LocalContext.current
    val paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = runCatching { ResourcesCompat.getFont(context, R.font.crimson_pro_bold) }.getOrNull()
        }
    }

    LaunchedEffect(rollKey) {
        flash.snapTo(0f)
        if (animate) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(ROLL_MILLIS, easing = LinearEasing))
            onSettled()
            if (natural20) {
                flash.animateTo(1f, tween(FLASH_MILLIS, easing = LinearEasing))
                flash.snapTo(0f)
            }
        } else {
            progress.snapTo(1f)
            onSettled()
        }
    }

    // Pontinhos do feltro, sorteados uma vez só
    val speckles = remember { Random(7).let { r -> List(900) { Offset(r.nextFloat(), r.nextFloat()) } } }

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val t = progress.value
        val settled = t >= 1f
        drawFelt(speckles)

        val shown = dice.take(MAX_SHOWN)
        val count = shown.size.coerceAtLeast(1)
        val cols = min(count, 3)
        val rows = ceil(count / cols.toFloat()).toInt()
        val scale = when (count) { 1 -> 1f; 2 -> 0.8f; 3 -> 0.68f; 4 -> 0.62f; 5 -> 0.55f; else -> 0.52f }
        val r = min(w, h) * 0.17f * scale
        val maxDelay = (shown.size - 1) * 0.05f

        // 20 natural: brilho rápido (sobe e some), sem mudar a cor do dado
        val f = flash.value
        val glow = when {
            f <= 0f -> 0f
            f < 0.2f -> f / 0.2f
            else -> (1 - (f - 0.2f) / 0.8f).pow(2)
        }

        shown.forEachIndexed { i, die ->
            val tumble = tumbles.getOrNull(i) ?: return@forEachIndexed
            // Posição final de cada dado numa grade no centro da bandeja
            val col = i % cols
            val row = i / cols
            val rowCount = if (row == rows - 1) shown.size - row * cols else cols
            val spacingX = min(w * 0.8f / cols, r * 2.8f)
            val spacingY = r * 2.6f
            val finalX = w / 2 + (col - (rowCount - 1) / 2f) * spacingX
            val baseY = h * 0.56f + (row - (rows - 1) / 2f) * spacingY - (if (rows > 1) r * 0.3f else 0f)

            // Cada dado sai um pouco depois do anterior; todos param juntos no fim
            val local = ((t - i * 0.05f) / (1 - maxDelay)).coerceIn(0f, 1f)
            val groundT = min(1f, local / 0.82f)
            val cx = finalX + (-0.75f - i * 0.05f) * w * (1 - easeOut(groundT))
            val lift = h * 0.32f * tumble.bounce * scale * (1 - groundT).pow(2) * abs(cos(PI.toFloat() * 3.5f * groundT))
            val ground = baseY + r
            val cy = baseY - lift
            val hn = min(1f, lift / (h * 0.32f))
            drawOval(
                Color.Black.copy(alpha = 0.42f * (1 - hn * 0.6f)),
                topLeft = Offset(cx + r * 0.15f - r * (1.05f - hn * 0.35f), ground - r * (0.3f - hn * 0.1f)),
                size = Size(2 * r * (1.05f - hn * 0.35f), 2 * r * (0.3f - hn * 0.1f)),
            )

            if (glow > 0f) {
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFF8EC).copy(alpha = 0.75f * glow), Color(0x00FFF8EC)),
                        center = Offset(cx, cy),
                        radius = r * (1.4f + 1.4f * f),
                    ),
                    radius = r * (1.4f + 1.4f * f),
                    center = Offset(cx, cy),
                )
            }

            val dim = settled && natural1
            val model = DiceModels.of(die.sides)
            if (model != null) {
                val landing = DiceModels.faceFor(die.sides, die.value)
                val e = easeOut(local).toDouble()
                // A face sorteada para de frente para quem olha
                val m = model.finalOrientation(landing, tumble.tilt) *
                    Mat3.rotation(tumble.axis1, tumble.turns1 * (1 - e)) *
                    Mat3.rotation(tumble.axis2, tumble.turns2 * (1 - e))
                drawSolid(model, m, cx, cy, r, dim, glow, paint) { number -> DiceModels.label(die.sides, number, landing, die.value) }
            } else {
                drawFlatDie(cx, cy, r, tumble.spin2d * (1 - easeOut(local)), if (local >= 1f) die.value.toString() else "", dim, paint)
            }
            if (glow > 0f) drawSparkle(cx + r * 0.62f, cy - r * 0.7f, r * 0.55f * glow, glow)
        }

        // Mais dados do que cabem: mostra quantos ficaram de fora
        val extra = dice.size - shown.size
        if (extra > 0 && settled) {
            drawIntoCanvas { canvas ->
                paint.textSize = h * 0.08f
                paint.color = android.graphics.Color.argb(220, 246, 236, 228)
                canvas.nativeCanvas.drawText("+$extra dados", w * 0.82f, h * 0.9f, paint)
            }
        }
    }
}

private fun easeOut(t: Float): Float = 1 - (1 - t).pow(3)

// ============================== Bandeja ==============================

private fun DrawScope.drawFelt(speckles: List<Offset>) {
    val w = size.width
    val h = size.height
    drawRect(
        Brush.radialGradient(
            listOf(Color(0xFF5E1C1C), Color(0xFF2C0C0C)),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = w * 0.7f,
        ),
    )
    drawPoints(speckles.filterIndexed { i, _ -> i % 2 == 0 }.map { Offset(it.x * w, it.y * h) }, PointMode.Points, Color.White.copy(alpha = 0.035f), 2f)
    drawPoints(speckles.filterIndexed { i, _ -> i % 2 == 1 }.map { Offset(it.x * w, it.y * h) }, PointMode.Points, Color.Black.copy(alpha = 0.08f), 2f)
    val rim = max(6f, w * 0.025f)
    drawRect(Color(0xFF1A0606), style = Stroke(width = rim * 2))
    drawRect(
        Color(0x8CC9971F),
        topLeft = Offset(rim * 1.6f, rim * 1.6f),
        size = Size(w - rim * 3.2f, h - rim * 3.2f),
        style = Stroke(width = 1.5f),
    )
}

private fun DrawScope.drawSparkle(x: Float, y: Float, s: Float, alpha: Float) {
    val color = Color.White.copy(alpha = alpha)
    fun star(rx: Float, ry: Float) = Path().apply {
        moveTo(x, y - ry); quadraticTo(x, y, x + rx, y)
        quadraticTo(x, y, x, y + ry); quadraticTo(x, y, x - rx, y)
        quadraticTo(x, y, x, y - ry); close()
    }
    drawPath(star(s * 0.32f, s), color)
    drawPath(star(s * 0.7f, s * 0.22f), color)
}

// ============================== Dados em 3D ==============================

private val LIGHT = Vec3(-0.35, 0.4, 0.85).normalized()
private val HALF = (LIGHT + Vec3(0.0, 0.0, 1.0)).normalized()
private const val DIST = 7.0
private const val DEPTH = 0.55
private val CAMERA = Vec3(0.0, 0.0, DIST / DEPTH)

private val BASE = floatArrayOf(158f, 27f, 31f)       // vermelho da resina
private val NUMBER = floatArrayOf(246f, 236f, 228f)   // cor de papel

private fun mix(a: FloatArray, b: FloatArray, t: Float) = FloatArray(3) { a[it] + (b[it] - a[it]) * t }
private fun color(c: FloatArray, alpha: Float = 1f) =
    Color(c[0].coerceIn(0f, 255f) / 255f, c[1].coerceIn(0f, 255f) / 255f, c[2].coerceIn(0f, 255f) / 255f, alpha)

private fun DrawScope.drawSolid(
    model: Polyhedron,
    m: Mat3,
    cx: Float,
    cy: Float,
    r: Float,
    dim: Boolean,
    glow: Float,
    paint: Paint,
    label: (Int) -> String,
) {
    fun project(p: Vec3): Offset {
        val s = (DIST / (DIST - p.z * DEPTH)).toFloat()
        return Offset(cx + p.x.toFloat() * r * s, cy - p.y.toFloat() * r * s)
    }
    val verts = model.vertices.map { m * it }

    class Drawn(val number: Int, val c: Vec3, val n: Vec3, val up: Vec3, val side: Vec3, val inradius: Double, val pts: List<Offset>)
    val visible = model.faces.map { f ->
        Drawn(f.number, m * f.center, m * f.normal, m * f.up, m * f.side, f.inradius, f.vertices.map { project(verts[it]) })
    }.filter { (it.n dot (CAMERA - it.c)) > 0 }.sortedBy { it.c.z }

    for (o in visible) {
        val d = max(0.0, o.n dot LIGHT).toFloat()
        val spec = max(0.0, o.n dot HALF).pow(28).toFloat() * 0.75f
        var c = FloatArray(3) { BASE[it] * (0.32f + 0.68f * d) * (if (dim) 0.55f else 1f) }
        c = mix(c, floatArrayOf(255f, 250f, 240f), spec)
        if (dim) c = mix(c, floatArrayOf(110f, 104f, 104f), 0.55f)
        c = mix(c, floatArrayOf(255f, 246f, 236f), glow * 0.45f)

        val path = Path().apply {
            moveTo(o.pts[0].x, o.pts[0].y)
            for (k in 1 until o.pts.size) lineTo(o.pts[k].x, o.pts[k].y)
            close()
        }
        drawPath(path, color(c))
        drawPath(path, color(mix(c, floatArrayOf(255f, 235f, 225f), 0.18f)), style = Stroke(width = max(0.8f, r * 0.012f), join = StrokeJoin.Round))

        // Número no plano da face, por uma transformação afim; o tamanho acompanha a face
        if (o.n.z < 0.12) continue
        val k = 0.87 * o.inradius
        val p0 = project(o.c)
        val px = project(o.c + o.side * -k)
        val py = project(o.c + o.up * -k)
        val unit = 100f
        val text = label(o.number)
        paint.textSize = when (text.length) { 1 -> 92f; 2 -> 78f; else -> 60f }
        // O número some aos poucos quando a face vira de lado
        val alpha = (min(1.0, (o.n.z - 0.12) * 4) * 255).toInt()
        val num = mix(NUMBER, floatArrayOf(0f, 0f, 0f), (1 - (0.55f + 0.45f * d)) * 0.6f)
        paint.color = android.graphics.Color.argb(alpha, num[0].toInt(), num[1].toInt(), num[2].toInt())
        val matrix = android.graphics.Matrix().apply {
            setValues(floatArrayOf(
                (px.x - p0.x) / unit, (py.x - p0.x) / unit, p0.x,
                (px.y - p0.y) / unit, (py.y - p0.y) / unit, p0.y,
                0f, 0f, 1f,
            ))
        }
        drawIntoCanvas { canvas ->
            val nc = canvas.nativeCanvas
            nc.save()
            nc.concat(matrix)
            val fm = paint.fontMetrics
            nc.drawText(text, 0f, -(fm.ascent + fm.descent) / 2 + 6f, paint)
            // Tracinho do 6 e do 9, como nos dados físicos (onde existe o 9 ou o 6 de cabeça para baixo)
            if ((text == "6" || text == "9") && model.sides >= 8) nc.drawRect(-16f, 48f, 16f, 55f, paint)
            nc.restore()
        }
    }
}

// ============================== Dados sem forma física ==============================

/** Dados como d3 ou d7, que não existem como sólido: forma chapada que gira, com o valor. */
private fun DrawScope.drawFlatDie(cx: Float, cy: Float, r: Float, angle: Float, text: String, dim: Boolean, paint: Paint) {
    val rr = r * 1.05f
    val pts = List(6) { i ->
        val a = Math.toRadians(-90.0 + 60.0 * i)
        Offset(cx + rr * cos(a).toFloat(), cy + rr * sin(a).toFloat())
    }
    val path = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) }; close() }
    var body = BASE
    if (dim) body = mix(body, floatArrayOf(110f, 104f, 104f), 0.55f)
    rotate(angle, Offset(cx, cy)) {
        drawPath(path, Brush.linearGradient(listOf(color(mix(body, floatArrayOf(255f, 240f, 230f), 0.18f)), color(FloatArray(3) { body[it] * 0.55f })),
            start = Offset(cx - rr, cy - rr), end = Offset(cx + rr, cy + rr)))
        drawPath(path, color(mix(body, floatArrayOf(255f, 235f, 225f), 0.25f)), style = Stroke(width = max(1f, r * 0.02f), join = StrokeJoin.Round))
    }
    if (text.isNotEmpty()) {
        drawIntoCanvas { canvas ->
            paint.textSize = r * (if (text.length > 2) 0.62f else 0.85f)
            paint.color = android.graphics.Color.rgb(NUMBER[0].toInt(), NUMBER[1].toInt(), NUMBER[2].toInt())
            val fm = paint.fontMetrics
            canvas.nativeCanvas.drawText(text, cx, cy - (fm.ascent + fm.descent) / 2, paint)
        }
    }
}
