package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.PersonalityPreset

object FeedbackAdapter {
    fun adapt(settings: AiSettings, signal: FeedbackSignal): AiSettings {
        if (!settings.learning.learningEnabled || !settings.learning.safeAutoTune) return settings

        val direction = if (signal == FeedbackSignal.POSITIVE) 1f else -1f
        val rawStep = settings.learning.learningRate * direction
        val step = rawStep.coerceIn(
            -settings.learning.maxAdjustmentPerFeedback,
            settings.learning.maxAdjustmentPerFeedback,
        )

        val p = settings.personality
        return settings.copy(
            personalityPreset = PersonalityPreset.CUSTOM,
            personality = p.copy(
                initiative = (p.initiative + step * 0.60f).coerceIn(0f, 1f),
                empathy = (p.empathy + step * 0.35f).coerceIn(0f, 1f),
                verbosity = (p.verbosity + step * 0.20f).coerceIn(0f, 1f),
                skepticism = (p.skepticism + step * 0.15f).coerceIn(0f, 1f),
            ),
        ).normalized()
    }
}
