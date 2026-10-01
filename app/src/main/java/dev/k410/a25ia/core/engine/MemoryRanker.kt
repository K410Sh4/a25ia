package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.MemoryRecord
import java.text.Normalizer
import kotlin.math.max

object MemoryRanker {
    fun rank(
        query: String,
        memories: List<MemoryRecord>,
        limit: Int,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<MemoryRecord> {
        val queryTokens = tokens(query)
        if (memories.isEmpty()) return emptyList()

        return memories
            .asSequence()
            .map { memory ->
                val memoryTokens = tokens(memory.text)
                val overlap = if (queryTokens.isEmpty()) {
                    0.0
                } else {
                    queryTokens.intersect(memoryTokens).size.toDouble() / queryTokens.size
                }
                val ageDays = max(0.0, (nowEpochMs - memory.createdAtEpochMs) / 86_400_000.0)
                val recency = 1.0 / (1.0 + ageDays / 30.0)
                val score = overlap * 0.65 + memory.importance.coerceIn(0f, 1f) * 0.20 + recency * 0.15
                memory to score
            }
            .sortedByDescending { it.second }
            .take(limit.coerceAtLeast(1))
            .map { it.first }
            .toList()
    }

    internal fun tokens(text: String): Set<String> {
        val normalized = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return normalized
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 2 }
            .toSet()
    }
}
