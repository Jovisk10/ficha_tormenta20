package com.jovis.t20.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/** Qual borda da faixa é rasgada. */
enum class TornStyle {
    /** Faixa fina rasgada dos dois lados, como um traço de pincel. */
    BAND,
    /** Borda de cima rasgada e a de baixo reta (para ficar em cima de um bloco). */
    TOP,
    /** Borda de baixo rasgada e a de cima reta (para ficar embaixo de um bloco). */
    BOTTOM,
}

/**
 * A borda de pincel rasgada das tabelas do livro.
 * O desenho é sempre o mesmo para a mesma semente, então não "pisca" ao redesenhar a tela.
 */
@Composable
fun TornEdge(
    color: Color,
    modifier: Modifier = Modifier,
    style: TornStyle = TornStyle.BAND,
    height: Dp = 8.dp,
    seed: Int = 3,
) {
    Canvas(modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val h = size.height
        val random = Random(seed)
        val step = 4.dp.toPx()
        val path = Path()

        // Borda de cima: reta (BOTTOM) ou irregular (BAND e TOP)
        path.moveTo(0f, if (style == TornStyle.BOTTOM) 0f else h * random.nextFloat() * 0.6f)
        var x = 0f
        while (x < w) {
            x = (x + step * (0.6f + random.nextFloat())).coerceAtMost(w)
            val y = if (style == TornStyle.BOTTOM) 0f else h * random.nextFloat() * (if (style == TornStyle.TOP) 0.9f else 0.6f)
            path.lineTo(x, y)
        }
        // Borda de baixo, voltando: reta (TOP) ou irregular (BAND e BOTTOM)
        while (x > 0f) {
            x = (x - step * (0.6f + random.nextFloat())).coerceAtLeast(0f)
            val y = if (style == TornStyle.TOP) h else h - h * random.nextFloat() * (if (style == TornStyle.BOTTOM) 0.9f else 0.5f)
            path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color)
    }
}
