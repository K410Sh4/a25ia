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
    fun feedbackChangesOnlyBoundedAdaptationAndKeepsBasePreset() {
        val settings = AiSettings(
            personalityPreset = PersonalityPreset.BALANCED,
            learning = LearningSettings(
                learningEnabled = true,
                safeAutoTune = true,
                learningRate = 0.5f,
                maxAdjustmentPerFeedback = 0.02f,
            ),
        )

        val updated = FeedbackAdapter.adapt(settings, FeedbackSignal.POSITIVE)

        assertEquals(settings.personality, updated.personality)
        assertEquals(PersonalityPreset.BALANCED, updated.personalityPreset)
        assertNotEquals(settings.adaptation, updated.adaptation)
        assertTrue(updated.adaptation.initiativeOffset <= 0.02f)
        assertTrue(updated.adaptation.verbosityOffset <= 0.02f)
    }
}
