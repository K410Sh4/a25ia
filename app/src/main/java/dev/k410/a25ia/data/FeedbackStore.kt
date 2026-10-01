package dev.k410.a25ia.data

import dev.k410.a25ia.core.model.FeedbackRecord

interface FeedbackStore {
    suspend fun add(record: FeedbackRecord, limit: Int = 500)
    suspend fun count(): Int
    suspend fun clear()
}
