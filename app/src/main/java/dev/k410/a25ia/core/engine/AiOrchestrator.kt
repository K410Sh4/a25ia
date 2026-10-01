package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.InferenceRequest
import dev.k410.a25ia.core.model.InferenceResult
import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRecord
import dev.k410.a25ia.core.model.MemoryRole
import dev.k410.a25ia.data.MemoryStore
import java.util.UUID

class AiOrchestrator(
    private val backend: InferenceBackend,
    private val memoryStore: MemoryStore,
) {
    suspend fun respond(message: String, settings: AiSettings): InferenceResult {
        val normalized = settings.normalized()
        val relevant = if (normalized.learning.memoryEnabled) {
            MemoryRanker.rank(
                query = message,
                memories = memoryStore.all(),
                limit = normalized.learning.memoryRetrievalCount,
            )
        } else {
            emptyList()
        }

        val result = backend.generate(
            InferenceRequest(
                userMessage = message,
                settings = normalized,
                memories = relevant,
            ),
        )

        if (normalized.learning.memoryEnabled) {
            storeConversation(message, result.text, normalized)
            if (normalized.learning.autoStoreFacts) {
                FactExtractor.extract(message).forEach { fact ->
                    memoryStore.add(
                        record = record(
                            kind = MemoryKind.FACT,
                            role = MemoryRole.USER,
                            text = fact,
                            importance = 0.9f,
                        ),
                        limit = normalized.learning.memoryLimit,
                    )
                }
            }
        }

        return result
    }

    suspend fun applyFeedback(
        responseText: String,
        signal: FeedbackSignal,
        settings: AiSettings,
    ): AiSettings {
        val updated = FeedbackAdapter.adapt(settings, signal)
        if (settings.learning.memoryEnabled) {
            memoryStore.add(
                record = record(
                    kind = MemoryKind.FEEDBACK,
                    role = MemoryRole.SYSTEM,
                    text = "${signal.name}: ${responseText.take(240)}",
                    importance = 0.65f,
                ),
                limit = settings.learning.memoryLimit,
            )
        }
        return updated
    }

    suspend fun memories(): List<MemoryRecord> =
        memoryStore.all().sortedByDescending { it.createdAtEpochMs }

    suspend fun clearMemory() = memoryStore.clear()

    private suspend fun storeConversation(user: String, assistant: String, settings: AiSettings) {
        memoryStore.add(
            record(MemoryKind.CONVERSATION, MemoryRole.USER, user, 0.50f),
            settings.learning.memoryLimit,
        )
        memoryStore.add(
            record(MemoryKind.CONVERSATION, MemoryRole.ASSISTANT, assistant, 0.40f),
            settings.learning.memoryLimit,
        )
    }

    private fun record(
        kind: MemoryKind,
        role: MemoryRole,
        text: String,
        importance: Float,
    ) = MemoryRecord(
        id = UUID.randomUUID().toString(),
        createdAtEpochMs = System.currentTimeMillis(),
        kind = kind,
        role = role,
        text = text.trim(),
        importance = importance.coerceIn(0f, 1f),
    )
}
