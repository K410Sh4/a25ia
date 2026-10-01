package dev.k410.a25ia.data

import android.content.Context
import dev.k410.a25ia.core.model.FeedbackRecord
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.PersonalityPreset
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class JsonFeedbackStore(context: Context) : FeedbackStore {
    private val file = File(context.filesDir, "a25ia_feedback.jsonl")
    private val mutex = Mutex()

    override suspend fun add(record: FeedbackRecord, limit: Int) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val existing = readAll()
                val kept = (existing + record).takeLast(limit.coerceIn(20, 2000))
                file.parentFile?.mkdirs()
                file.writeText(kept.joinToString("\n", transform = ::encode))
            }
        }
    }

    override suspend fun count(): Int = mutex.withLock {
        withContext(Dispatchers.IO) { readAll().size }
    }

    override suspend fun clear() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                if (file.exists()) file.delete()
            }
        }
    }

    private fun readAll(): List<FeedbackRecord> {
        if (!file.exists()) return emptyList()
        return file.useLines { lines -> lines.mapNotNull(::decode).toList() }
    }

    private fun encode(record: FeedbackRecord): String = JSONObject()
        .put("id", record.id)
        .put("createdAtEpochMs", record.createdAtEpochMs)
        .put("signal", record.signal.name)
        .put("responseSummary", record.responseSummary)
        .put("personalityPreset", record.personalityPreset.name)
        .toString()

    private fun decode(line: String): FeedbackRecord? = runCatching {
        val json = JSONObject(line)
        FeedbackRecord(
            id = json.getString("id"),
            createdAtEpochMs = json.getLong("createdAtEpochMs"),
            signal = FeedbackSignal.valueOf(json.getString("signal")),
            responseSummary = json.getString("responseSummary"),
            personalityPreset = PersonalityPreset.valueOf(json.getString("personalityPreset")),
        )
    }.getOrNull()
}
