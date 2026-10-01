package dev.k410.a25ia.data

import android.content.Context
import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRecord
import java.io.File
import java.text.Normalizer
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class JsonMemoryStore(context: Context) : MemoryStore {
    private val file = File(context.filesDir, "a25ia_long_term_memory.jsonl")
    private val legacyFile = File(context.filesDir, "a25ia_memory.jsonl")
    private val migrationMarker = File(context.filesDir, ".memory_v2_migrated")
    private val mutex = Mutex()

    override suspend fun all(): List<MemoryRecord> = mutex.withLock {
        withContext(Dispatchers.IO) {
            migrateLegacyIfNeeded()
            readCurrent()
        }
    }

    override suspend fun upsert(record: MemoryRecord, limit: Int) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                migrateLegacyIfNeeded()
                val existing = readCurrent().toMutableList()
                val index = existing.indexOfFirst { it.key == record.key }

                if (index >= 0) {
                    val previous = existing[index]
                    existing[index] = record.copy(
                        id = previous.id,
                        createdAtEpochMs = previous.createdAtEpochMs,
                    )
                } else {
                    existing += record
                }

                val kept = existing
                    .sortedByDescending { it.updatedAtEpochMs }
                    .take(limit.coerceIn(50, 5000))
                    .sortedBy { it.createdAtEpochMs }

                writeCurrent(kept)
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

    private fun migrateLegacyIfNeeded() {
        if (migrationMarker.exists()) return

        if (legacyFile.exists() && !file.exists()) {
            val migrated = legacyFile.useLines { lines ->
                lines.mapNotNull(::decodeLegacyFact).toList()
            }
            if (migrated.isNotEmpty()) {
                writeCurrent(migrated.distinctBy { it.key })
            }
        }

        migrationMarker.writeText("v2")
    }

    private fun readCurrent(): List<MemoryRecord> {
        if (!file.exists()) return emptyList()
        return file.useLines { lines -> lines.mapNotNull(::decode).toList() }
    }

    private fun writeCurrent(records: List<MemoryRecord>) {
        file.parentFile?.mkdirs()
        file.writeText(records.joinToString("\n", transform = ::encode))
    }

    private fun encode(record: MemoryRecord): String = JSONObject()
        .put("id", record.id)
        .put("key", record.key)
        .put("createdAtEpochMs", record.createdAtEpochMs)
        .put("updatedAtEpochMs", record.updatedAtEpochMs)
        .put("kind", record.kind.name)
        .put("text", record.text)
        .put("importance", record.importance.toDouble())
        .toString()

    private fun decode(line: String): MemoryRecord? = runCatching {
        val json = JSONObject(line)
        val created = json.getLong("createdAtEpochMs")
        MemoryRecord(
            id = json.getString("id"),
            key = json.getString("key"),
            createdAtEpochMs = created,
            updatedAtEpochMs = json.optLong("updatedAtEpochMs", created),
            kind = MemoryKind.valueOf(json.getString("kind")),
            text = json.getString("text"),
            importance = json.optDouble("importance", 0.5).toFloat(),
        )
    }.getOrNull()

    private fun decodeLegacyFact(line: String): MemoryRecord? = runCatching {
        val json = JSONObject(line)
        if (json.optString("kind") != "FACT") return@runCatching null

        val text = json.optString("text").trim()
        if (text.isBlank()) return@runCatching null

        val created = json.optLong("createdAtEpochMs", System.currentTimeMillis())
        MemoryRecord(
            id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
            key = "legacy:${normalizeKey(text)}",
            createdAtEpochMs = created,
            updatedAtEpochMs = created,
            kind = MemoryKind.FACT,
            text = text,
            importance = json.optDouble("importance", 0.7).toFloat().coerceIn(0f, 1f),
        )
    }.getOrNull()

    private fun normalizeKey(value: String): String =
        Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(100)
}
