package com.jovis.t20.rules

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Geometria dos dados usados na animação 3D: d4, d6, d8, d10, d12 e d20, numerados como dados
 * físicos (faces opostas somam lados + 1, exceto no d4, que não tem faces opostas).
 * As faces são encontradas a partir dos vértices, para qualquer sólido convexo.
 * Convenção: x para a direita, y para cima, z para fora da tela (em direção a quem olha).
 */

data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(k: Double) = Vec3(x * k, y * k, z * k)
    operator fun unaryMinus() = Vec3(-x, -y, -z)
    infix fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
    infix fun cross(o: Vec3) = Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    val length: Double get() = sqrt(this dot this)
    fun normalized(): Vec3 = this * (1.0 / length)
}

/** Matriz 3x3 de rotação, guardada por linhas. */
class Mat3(private val m: DoubleArray) {
    operator fun times(o: Mat3): Mat3 = Mat3(DoubleArray(9) { i ->
        val r = i / 3; val c = i % 3
        m[r * 3] * o.m[c] + m[r * 3 + 1] * o.m[3 + c] + m[r * 3 + 2] * o.m[6 + c]
    })

    operator fun times(v: Vec3) = Vec3(
        m[0] * v.x + m[1] * v.y + m[2] * v.z,
        m[3] * v.x + m[4] * v.y + m[5] * v.z,
        m[6] * v.x + m[7] * v.y + m[8] * v.z,
    )

    companion object {
        val IDENTITY = Mat3(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0))

        /** Rotação de [angle] radianos em torno de [axis]. */
        fun rotation(axis: Vec3, angle: Double): Mat3 {
            val a = axis.normalized()
            val c = cos(angle); val s = sin(angle); val t = 1 - c
            return Mat3(doubleArrayOf(
                c + a.x * a.x * t, a.x * a.y * t - a.z * s, a.x * a.z * t + a.y * s,
                a.y * a.x * t + a.z * s, c + a.y * a.y * t, a.y * a.z * t - a.x * s,
                a.z * a.x * t - a.y * s, a.z * a.y * t + a.x * s, c + a.z * a.z * t,
            ))
        }

        /** Matriz cujas linhas são os três vetores. */
        fun rows(a: Vec3, b: Vec3, c: Vec3) = Mat3(doubleArrayOf(a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z))
    }
}

/**
 * Uma face do dado. [up] define o "para cima" do número; [side] completa a moldura (normal × up).
 * [inradius] é a distância do centro à borda mais próxima, usada para dimensionar o número.
 */
data class DieFace(
    val vertices: List<Int>,
    val center: Vec3,
    val normal: Vec3,
    val up: Vec3,
    val side: Vec3,
    val inradius: Double,
    val number: Int,
)

/** Um dado como sólido convexo: vértices e faces numeradas. */
class Polyhedron(val sides: Int, rawVertices: List<Vec3>, radius: Double) {
    /** Vértices ajustados para que o dado fique com o raio pedido. */
    val vertices: List<Vec3> = rawVertices.maxOf { it.length }.let { max -> rawVertices.map { it * (radius / max) } }
    val faces: List<DieFace> = buildFaces()

    init {
        require(faces.size == sides) { "Esperadas $sides faces, encontradas ${faces.size}." }
    }

    fun face(number: Int): DieFace = faces.first { it.number == number }

    /**
     * Orientação final: a face [number] de frente para quem olha, com o número em pé,
     * girada [tilt] radianos no plano da tela para não parar sempre igual.
     */
    fun finalOrientation(number: Int, tilt: Double = 0.0): Mat3 {
        val f = face(number)
        // Leva up → (0,1,0), side → (−1,0,0), normal → (0,0,1)
        val target = Mat3(doubleArrayOf(0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0))
        return Mat3.rotation(Vec3(0.0, 0.0, 1.0), tilt) * target * Mat3.rows(f.up, f.side, f.normal)
    }

    private fun buildFaces(): List<DieFace> {
        val eps = 1e-6
        val found = mutableListOf<Set<Int>>()
        val n = vertices.size
        // Uma face é um plano com todos os outros vértices de um lado só
        for (i in 0 until n) for (j in i + 1 until n) for (k in j + 1 until n) {
            var normal = (vertices[j] - vertices[i]) cross (vertices[k] - vertices[i])
            if (normal.length < eps) continue
            normal = normal.normalized()
            val d = normal dot vertices[i]
            val dists = vertices.map { (normal dot it) - d }
            val onPlane = dists.indices.filter { abs(dists[it]) < eps }.toSet()
            val allBelow = dists.all { it < eps }
            val allAbove = dists.all { it > -eps }
            if ((allBelow || allAbove) && onPlane !in found) found += onPlane
        }

        val shapes = found.map { set ->
            val pts = set.toList()
            val center = pts.map { vertices[it] }.reduce(Vec3::plus) * (1.0 / pts.size)
            val any = (vertices[pts[1]] - vertices[pts[0]]) cross (vertices[pts[2]] - vertices[pts[0]])
            val normal = (if (any dot center < 0) -any else any).normalized()
            // Ordena os vértices no sentido anti-horário, visto de fora
            val e1 = (vertices[pts[0]] - center).normalized()
            val e2 = normal cross e1
            val ordered = pts.sortedBy { val p = vertices[it] - center; atan2(p dot e2, p dot e1) }

            // O número aponta para o vértice mais distante do centro (a ponta da pipa no d10);
            // em faces quadradas, aponta para o meio de uma aresta, para ficar em pé
            val dists = ordered.map { (vertices[it] - center).length }
            val regular = dists.maxOrNull()!! - dists.minOrNull()!! < 1e-6
            val up = if (regular && ordered.size == 4) {
                ((vertices[ordered[0]] + vertices[ordered[1]]) * 0.5 - center).normalized()
            } else {
                (vertices[ordered[dists.indexOf(dists.maxOrNull()!!)]] - center).normalized()
            }
            val inradius = ordered.indices.minOf { idx ->
                val a = vertices[ordered[idx]]; val b = vertices[ordered[(idx + 1) % ordered.size]]
                val ab = (b - a).normalized()
                val ac = center - a
                (ac - ab * (ac dot ab)).length
            }
            Triple(ordered, center, Pair(normal, Pair(up, inradius)))
        }

        // Numeração: faces opostas somam lados + 1; ordem fixa para o resultado ser sempre o mesmo
        val order = shapes.indices.sortedWith(compareBy({ -shapes[it].third.first.y }, { shapes[it].third.first.x }, { shapes[it].third.first.z }))
        val numbers = IntArray(shapes.size)
        var next = 1
        for (i in order) {
            if (numbers[i] != 0) continue
            val ni = shapes[i].third.first
            val opposite = shapes.indices.firstOrNull { j -> j != i && numbers[j] == 0 && (shapes[j].third.first dot ni) < -0.999 }
            numbers[i] = next
            if (opposite != null) numbers[opposite] = sides + 1 - next
            next++
            // Sem faces opostas (d4): numeração sequencial
            if (opposite == null) while (next <= sides && numbers.contains(next)) next++
        }
        if (next <= sides) {
            // Garante números únicos quando algumas faces não têm oposta
            val used = numbers.toMutableSet()
            for (i in numbers.indices) if (numbers[i] == 0) numbers[i] = (1..sides).first { it !in used }.also { used += it }
        }

        return shapes.mapIndexed { i, (ordered, center, rest) ->
            val (normal, upIn) = rest
            val (up, inradius) = upIn
            DieFace(ordered, center, normal, up, normal cross up, inradius, numbers[i])
        }
    }
}

object DiceModels {
    private val PHI = (1 + sqrt(5.0)) / 2

    val D4 = Polyhedron(4, listOf(Vec3(1.0, 1.0, 1.0), Vec3(1.0, -1.0, -1.0), Vec3(-1.0, 1.0, -1.0), Vec3(-1.0, -1.0, 1.0)), 1.75)

    val D6 = Polyhedron(6, buildList {
        for (x in listOf(-1.0, 1.0)) for (y in listOf(-1.0, 1.0)) for (z in listOf(-1.0, 1.0)) add(Vec3(x, y, z))
    }, 1.7)

    val D8 = Polyhedron(8, listOf(
        Vec3(1.0, 0.0, 0.0), Vec3(-1.0, 0.0, 0.0), Vec3(0.0, 1.0, 0.0),
        Vec3(0.0, -1.0, 0.0), Vec3(0.0, 0.0, 1.0), Vec3(0.0, 0.0, -1.0),
    ), 1.8)

    /** Trapezoedro pentagonal: duas pontas e um anel em zigue-zague, com faces em forma de pipa. */
    val D10 = run {
        val h = 1.15
        val c = cos(Math.toRadians(36.0))
        val a = h * (1 - c) / (1 + c)   // altura do anel que deixa cada pipa plana
        val pts = mutableListOf(Vec3(0.0, 0.0, h), Vec3(0.0, 0.0, -h))
        for (k in 0 until 5) {
            val up = Math.toRadians(72.0 * k)
            val down = Math.toRadians(72.0 * k + 36)
            pts += Vec3(cos(up), sin(up), a)
            pts += Vec3(cos(down), sin(down), -a)
        }
        Polyhedron(10, pts, 1.85)
    }

    val D12 = Polyhedron(12, buildList {
        for (x in listOf(-1.0, 1.0)) for (y in listOf(-1.0, 1.0)) for (z in listOf(-1.0, 1.0)) add(Vec3(x, y, z))
        for (a in listOf(-1.0, 1.0)) for (b in listOf(-1.0, 1.0)) {
            add(Vec3(0.0, a / PHI, b * PHI)); add(Vec3(a / PHI, b * PHI, 0.0)); add(Vec3(b * PHI, 0.0, a / PHI))
        }
    }, 1.9)

    val D20 = Polyhedron(20, buildList {
        for (a in listOf(-1.0, 1.0)) for (b in listOf(-1.0, 1.0)) {
            add(Vec3(0.0, a, b * PHI)); add(Vec3(a, b * PHI, 0.0)); add(Vec3(b * PHI, 0.0, a))
        }
    }, 1.9)

    /** O sólido de cada dado; o d100 usa o formato do d10. Null para dados sem forma física (ex.: d3). */
    fun of(sides: Int): Polyhedron? = when (sides) {
        4 -> D4; 6 -> D6; 8 -> D8; 10, 100 -> D10; 12 -> D12; 20 -> D20
        else -> null
    }

    /** Face em que o dado para para mostrar [value] (no d100, a face das dezenas correspondente). */
    fun faceFor(sides: Int, value: Int): Int = if (sides == 100) ((value - 1) / 10) + 1 else value

    /** Texto de cada face: no d100, dezenas (10, 20...) e o valor exato na face sorteada. */
    fun label(sides: Int, faceNumber: Int, landingFace: Int, value: Int): String = when {
        sides == 100 && faceNumber == landingFace -> value.toString()
        sides == 100 -> (faceNumber * 10).toString()
        else -> faceNumber.toString()
    }
}
