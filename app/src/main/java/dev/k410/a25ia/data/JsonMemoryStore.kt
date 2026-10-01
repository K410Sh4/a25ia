package dev.k410.a25ia.data

import android.content.Context
import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRecord
import dev.k410.a25ia.core.model.MemoryRole
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class JsonMemoryStore(context: Context) : MemoryStore {
    private val file = File(context.filesDir, "a25ia_memory.jsonl")
    private val mutex = Mutex()

    override suspend fun all(): List<MemoryRecord> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext emptyList()
            file.useLines { lines ->
                lines.mapNotNull(::decode).toList()
            }
        }
    }

    override suspend fun add(record: MemoryRecord, limit: Int) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val existing = if (file.exists()) {
                    file.useLines { lines -> lines.mapNotNull(::decode).toList() }
                } else {
                    emptyList()
                }
                val kept = (existing + record)
                    .takeLast(limit.coerceIn(50, 5000))
                file.parentFile?.mkdirs()
                file.writeText(kept.joinToString("\n", transform = ::encode))
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                if (file.exists()) file.delete()
            }
        }
    }

    private fun encode(record: MemoryRecord): String = JSONObject()
        .put("id", record.id)
        .put("createdAtEpochMs", record.createdAtEpochMs)
        .put("kind", record.kind.name)
        .put("role", record.role.name)
        .put("text", record.text)
        .put("importance", record.importance.toDouble())
        .toString()

    private fun decode(line: String): MemoryRecord? = runCatching {
        val json = JSONObject(line)
        MemoryRecord(
            id = json.getString("id"),
            createdAtEpochMs = json.getLong("createdAtEpochMs"),
            kind = MemoryKind.valueOf(json.getString("kind")),
            role = MemoryRole.valueOf(json.getString("role")),
            text = json.getString("text"),
            importance = json.optDouble("importance", 0.5).toFloat(),
        )
    }.getOrNull()
}
