package dev.k410.a25ia.core.model

data class MemoryRecord(
    val id: String,
    val createdAtEpochMs: Long,
    val kind: MemoryKind,
    val role: MemoryRole,
    val text: String,
    val importance: Float = 0.5f,
)

enum class MemoryKind {
    CONVERSATION,
    FACT,
    FEEDBACK,
}

enum class MemoryRole {
    USER,
    ASSISTANT,
    SYSTEM,
}
