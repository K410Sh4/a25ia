package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackSignal

object FeedbackAdapter {
    fun adapt(settings: AiSettings, signal: FeedbackSignal): AiSettings {
        if (!settings.learning.learningEnabled || !settings.learning.safeAutoTune) return settings

        val direction = if (signal == FeedbackSignal.POSITIVE) 1f else -1f
        val rawStep = settings.learning.learningRate * direction
        val step = rawStep.coerceIn(
            -settings.learning.maxAdjustmentPerFeedback,
            settings.learning.maxAdjustmentPerFeedback,
        )

        val current = settings.adaptation
        return settings.copy(
            adaptation = current.copy(
                initiativeOffset = current.initiativeOffset + step * 0.60f,
                empathyOffset = current.empathyOffset + step * 0.35f,
                verbosityOffset = current.verbosityOffset + step * 0.20f,
                skepticismOffset = current.skepticismOffset + step * 0.15f,
            ),
        ).normalized()
    }
}
