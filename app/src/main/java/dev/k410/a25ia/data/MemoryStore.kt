package dev.k410.a25ia.data

import dev.k410.a25ia.core.model.MemoryRecord

interface MemoryStore {
    suspend fun all(): List<MemoryRecord>
    suspend fun add(record: MemoryRecord, limit: Int)
    suspend fun clear()
}
