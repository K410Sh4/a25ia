package dev.k410.a25ia

import android.content.Context
import dev.k410.a25ia.core.engine.AiOrchestrator
import dev.k410.a25ia.core.engine.LocalAdaptiveBackend
import dev.k410.a25ia.core.engine.SessionMemory
import dev.k410.a25ia.data.JsonFeedbackStore
import dev.k410.a25ia.data.JsonMemoryStore
import dev.k410.a25ia.data.SettingsRepository

class AppContainer(context: Context) {
    val settingsRepository = SettingsRepository(context)

    private val memoryStore = JsonMemoryStore(context)
    private val feedbackStore = JsonFeedbackStore(context)
    private val sessionMemory = SessionMemory()
    private val inferenceBackend = LocalAdaptiveBackend()

    val orchestrator = AiOrchestrator(
        backend = inferenceBackend,
        memoryStore = memoryStore,
        feedbackStore = feedbackStore,
        sessionMemory = sessionMemory,
    )
}
