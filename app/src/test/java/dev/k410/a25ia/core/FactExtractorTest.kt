package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.FactExtractor
import dev.k410.a25ia.core.model.MemoryKind
import org.junit.Assert.assertEquals
import org.junit.Test

class FactExtractorTest {
    @Test
    fun extractsStructuredPortugueseMemories() {
        val result = FactExtractor.extract(
            "Meu nome é Kaio. Eu gosto de programação. Meu objetivo é aprender IA.",
        )

        assertEquals(3, result.size)
        assertEquals("identity:name", result[0].key)
        assertEquals(MemoryKind.FACT, result[0].kind)
        assertEquals("Nome: Kaio", result[0].text)
        assertEquals(MemoryKind.PREFERENCE, result[1].kind)
        assertEquals("Gosta de: programação", result[1].text)
        assertEquals(MemoryKind.GOAL, result[2].kind)
        assertEquals("Objetivo: aprender IA", result[2].text)
    }
}
