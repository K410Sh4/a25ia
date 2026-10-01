package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.FeedbackAdapter
import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.LearningSettings
import dev.k410.a25ia.core.model.PersonalityPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackAdapterTest {
    @Test
    fun feedbackDoesNothingWhenLearningIsDisabled() {
        val settings = AiSettings(
            learning = LearningSettings(learningEnabled = false),
        )

        assertEquals(settings, FeedbackAdapter.adapt(settings, FeedbackSignal.POSITIVE))
    }

    @Test
    fun positiveFeedbackUsesBoundedAdjustment() {
        val settings = AiSettings(
            learning = LearningSettings(
                learningEnabled = true,
                safeAutoTune = true,
                learningRate = 0.5f,
                maxAdjustmentPerFeedback = 0.02f,
            ),
        )

        val updated = FeedbackAdapter.adapt(settings, FeedbackSignal.POSITIVE)

        assertNotEquals(settings.personality, updated.personality)
        assertEquals(PersonalityPreset.CUSTOM, updated.personalityPreset)
        assertTrue(updated.personality.initiative - settings.personality.initiative <= 0.02f)
    }
}
