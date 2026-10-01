package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackRecord
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.InferenceRequest
import dev.k410.a25ia.core.model.InferenceResult
import dev.k410.a25ia.core.model.MemoryRecord
import dev.k410.a25ia.core.model.MemoryRole
import dev.k410.a25ia.core.model.MemorySnapshot
import dev.k410.a25ia.core.model.UserIntent
import dev.k410.a25ia.data.FeedbackStore
import dev.k410.a25ia.data.MemoryStore
import java.util.UUID

class AiOrchestrator(
    private val backend: InferenceBackend,
    private val memoryStore: MemoryStore,
    private val feedbackStore: FeedbackStore,
    private val sessionMemory: SessionMemory,
) {
    suspend fun respond(message: String, settings: AiSettings): InferenceResult {
        val normalized = settings.normalized()
        val intent = IntentClassifier.classify(message)
        val longTerm = if (normalized.learning.memoryEnabled) memoryStore.all() else emptyList()

        val relevant = when {
            !normalized.learning.memoryEnabled -> emptyList()
            intent == UserIntent.MEMORY -> MemoryRanker.overview(
                memories = longTerm,
                limit = normalized.learning.memoryRetrievalCount,
            )
            else -> MemoryRanker.rank(
                query = message,
                memories = longTerm,
                limit = normalized.learning.memoryRetrievalCount,
            )
        }

        val sessionContext = sessionMemory.snapshot(normalized.generation.contextMessages)

        val result = backend.generate(
            InferenceRequest(
                userMessage = message,
                settings = normalized,
                intent = intent,
                sessionContext = sessionContext,
                memories = relevant,
            ),
        )

        val sessionLimit = (normalized.generation.contextMessages * 2).coerceIn(4, 128)
        sessionMemory.append(MemoryRole.USER, message, sessionLimit)
        sessionMemory.append(MemoryRole.ASSISTANT, result.text, sessionLimit)

        if (normalized.learning.memoryEnabled && normalized.learning.autoStoreFacts) {
            FactExtractor.extract(message).forEach { extracted ->
                val now = System.currentTimeMillis()
                memoryStore.upsert(
                    record = MemoryRecord(
                        id = UUID.randomUUID().toString(),
                        key = extracted.key,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                        kind = extracted.kind,
                        text = extracted.text,
                        importance = extracted.importance,
                    ),
                    limit = normalized.learning.memoryLimit,
                )
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

        feedbackStore.add(
            FeedbackRecord(
                id = UUID.randomUUID().toString(),
                createdAtEpochMs = System.currentTimeMillis(),
                signal = signal,
                responseSummary = responseText
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(180),
                personalityPreset = settings.personalityPreset,
            ),
        )

        return updated
    }

    suspend fun memorySnapshot(): MemorySnapshot =
        MemorySnapshot(
            longTermMemories = memoryStore.all().sortedByDescending { it.updatedAtEpochMs },
            sessionTurnCount = sessionMemory.size(),
            feedbackCount = feedbackStore.count(),
        )

    suspend fun clearMemory() {
        memoryStore.clear()
        feedbackStore.clear()
        sessionMemory.clear()
    }
}
