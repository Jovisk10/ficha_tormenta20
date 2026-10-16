package com.jovis.t20.rules

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiceModelsTest {

    private val all = listOf(DiceModels.D4, DiceModels.D6, DiceModels.D8, DiceModels.D10, DiceModels.D12, DiceModels.D20)

    @Test
    fun `cada dado tem o numero certo de faces numeradas de 1 a N`() {
        all.forEach { die ->
            assertEquals(die.sides, die.faces.size, "d${die.sides}")
            assertEquals((1..die.sides).toSet(), die.faces.map { it.number }.toSet(), "d${die.sides}")
        }
    }

    @Test
    fun `faces sao planas e apontam para fora`() {
        all.forEach { die ->
            die.faces.forEach { f ->
                assertTrue((f.normal dot f.center) > 0, "d${die.sides}: normal para dentro")
                f.vertices.forEach { v ->
                    assertTrue(abs((die.vertices[v] - f.center) dot f.normal) < 1e-9, "d${die.sides}: face torta")
                }
            }
        }
    }

    @Test
    fun `faces opostas somam lados mais 1`() {
        listOf(DiceModels.D6, DiceModels.D8, DiceModels.D10, DiceModels.D12, DiceModels.D20).forEach { die ->
            die.faces.forEach { f ->
                val opposite = die.faces.first { (it.normal dot f.normal) < -0.999 }
                assertEquals(die.sides + 1, f.number + opposite.number, "d${die.sides}")
            }
        }
    }

    @Test
    fun `orientacao final mostra a face sorteada de frente e em pe`() {
        all.forEach { die ->
            for (n in 1..die.sides) {
                val m = die.finalOrientation(n, tilt = 0.0)
                val f = die.face(n)
                assertTrue(abs((m * f.normal).z - 1) < 1e-9, "d${die.sides}, face $n não ficou de frente")
                assertTrue(abs((m * f.up).y - 1) < 1e-9, "d${die.sides}, face $n não ficou em pé")
            }
        }
    }

    @Test
    fun `numero no d10 aponta para a ponta da pipa e no d6 fica reto`() {
        DiceModels.D10.faces.forEach { f ->
            val tip = f.vertices.maxBy { (DiceModels.D10.vertices[it] - f.center).length }
            assertTrue(((DiceModels.D10.vertices[tip] - f.center).normalized() dot f.up) > 0.999)
        }
        DiceModels.D6.faces.forEach { f ->
            // O "para cima" de uma face do cubo é paralelo a uma aresta, não diagonal
            val edge = (DiceModels.D6.vertices[f.vertices[1]] - DiceModels.D6.vertices[f.vertices[0]]).normalized()
            assertTrue(abs(edge dot f.up) < 1e-9)
        }
    }

    @Test
    fun `d100 usa o d10 e mostra o valor exato na face sorteada`() {
        assertEquals(DiceModels.D10, DiceModels.of(100))
        assertEquals(5, DiceModels.faceFor(100, 47))
        assertEquals(10, DiceModels.faceFor(100, 100))
        assertEquals("47", DiceModels.label(100, 5, 5, 47))
        assertEquals("30", DiceModels.label(100, 3, 5, 47))
        assertNull(DiceModels.of(3))
        assertNotNull(DiceModels.of(12))
    }

    @Test
    fun `rotacao preserva o tamanho dos vetores`() {
        val m = Mat3.rotation(Vec3(1.0, 2.0, 3.0), 1.234)
        DiceModels.D20.vertices.forEach { v -> assertTrue(abs((m * v).length - v.length) < 1e-9) }
    }
}
