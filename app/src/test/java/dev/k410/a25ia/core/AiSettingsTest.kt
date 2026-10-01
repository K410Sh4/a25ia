package dev.k410.a25ia.core

import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.GenerationSettings
import dev.k410.a25ia.core.model.LearningSettings
import dev.k410.a25ia.core.model.PersonalityAdaptation
import dev.k410.a25ia.core.model.PersonalitySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiSettingsTest {
    @Test
    fun normalizedClampsUnsafeValues() {
        val normalized = AiSettings(
            assistantName = "",
            generation = GenerationSettings(
                temperature = 99f,
                topP = -1f,
                topK = 0,
                maxOutputTokens = 99_999,
                contextMessages = 0,
            ),
            personality = PersonalitySettings(creativity = 2f, skepticism = -2f),
            adaptation = PersonalityAdaptation(
                verbosityOffset = 2f,
                initiativeOffset = -2f,
            ),
            learning = LearningSettings(
                learningRate = 5f,
                maxAdjustmentPerFeedback = 2f,
                memoryRetrievalCount = 0,
                memoryLimit = 1,
            ),
        ).normalized()

        assertEquals("A25IA", normalized.assistantName)
        assertEquals(2f, normalized.generation.temperature)
        assertEquals(0.05f, normalized.generation.topP)
        assertEquals(1, normalized.generation.topK)
        assertEquals(4096, normalized.generation.maxOutputTokens)
        assertEquals(1f, normalized.personality.creativity)
        assertEquals(0f, normalized.personality.skepticism)
        assertEquals(0.25f, normalized.adaptation.verbosityOffset)
        assertEquals(-0.25f, normalized.adaptation.initiativeOffset)
        assertTrue(normalized.learning.learningRate <= 0.5f)
        assertTrue(normalized.learning.maxAdjustmentPerFeedback <= 0.2f)
        assertEquals(50, normalized.learning.memoryLimit)
    }
}
