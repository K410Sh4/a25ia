package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.FactExtractor
import org.junit.Assert.assertEquals
import org.junit.Test

class FactExtractorTest {
    @Test
    fun extractsExplicitPortugueseFacts() {
        val result = FactExtractor.extract("Meu nome é Kaio. Eu gosto de programação.")

        assertEquals(listOf("Kaio", "programação"), result)
    }
}
