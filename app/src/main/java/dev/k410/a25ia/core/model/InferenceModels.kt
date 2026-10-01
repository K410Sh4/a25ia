package dev.k410.a25ia.core.model

data class InferenceRequest(
    val userMessage: String,
    val settings: AiSettings,
    val intent: UserIntent,
    val sessionContext: List<SessionTurn>,
    val memories: List<MemoryRecord>,
)

data class InferenceResult(
    val text: String,
    val backendId: String,
    val latencyMs: Long,
    val diagnostics: Map<String, String> = emptyMap(),
)

enum class FeedbackSignal {
    POSITIVE,
    NEGATIVE,
}

enum class UserIntent {
    IDENTITY,
    MEMORY,
    CONFIGURATION,
    GREETING,
    QUESTION,
    STATEMENT,
}
