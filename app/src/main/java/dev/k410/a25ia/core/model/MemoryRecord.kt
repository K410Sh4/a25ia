package dev.k410.a25ia.core.model

data class MemoryRecord(
    val id: String,
    val key: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val kind: MemoryKind,
    val text: String,
    val importance: Float = 0.5f,
)

enum class MemoryKind {
    FACT,
    PREFERENCE,
    GOAL,

    // Kept only so legacy v0.1 records can be decoded during migration.
    CONVERSATION,
    FEEDBACK,
}

data class SessionTurn(
    val role: MemoryRole,
    val text: String,
)

enum class MemoryRole {
    USER,
    ASSISTANT,
    SYSTEM,
}

data class FeedbackRecord(
    val id: String,
    val createdAtEpochMs: Long,
    val signal: FeedbackSignal,
    val responseSummary: String,
    val personalityPreset: PersonalityPreset,
)

data class MemorySnapshot(
    val longTermMemories: List<MemoryRecord>,
    val sessionTurnCount: Int,
    val feedbackCount: Int,
)
