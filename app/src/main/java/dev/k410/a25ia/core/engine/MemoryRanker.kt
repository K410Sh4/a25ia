package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRecord
import java.text.Normalizer
import kotlin.math.max
import kotlin.math.sqrt

object MemoryRanker {
    private const val MIN_SIMILARITY = 0.28

    fun rank(
        query: String,
        memories: List<MemoryRecord>,
        limit: Int,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<MemoryRecord> {
        val queryTokens = tokens(query)
        if (queryTokens.isEmpty()) return emptyList()

        return memories
            .asSequence()
            .filter(::eligible)
            .mapNotNull { memory ->
                val memoryTokens = tokens(memory.text)
                if (memoryTokens.isEmpty()) return@mapNotNull null

                val intersection = queryTokens.intersect(memoryTokens).size.toDouble()
                val similarity = intersection / sqrt(queryTokens.size.toDouble() * memoryTokens.size.toDouble())
                if (similarity < MIN_SIMILARITY) return@mapNotNull null

                val ageDays = max(0.0, (nowEpochMs - memory.updatedAtEpochMs) / 86_400_000.0)
                val recency = 1.0 / (1.0 + ageDays / 45.0)
                val score =
                    similarity * 0.80 +
                    memory.importance.coerceIn(0f, 1f) * 0.12 +
                    recency * 0.08

                memory to score
            }
            .sortedByDescending { it.second }
            .take(limit.coerceAtLeast(1))
            .map { it.first }
            .toList()
    }

    fun overview(
        memories: List<MemoryRecord>,
        limit: Int,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): List<MemoryRecord> =
        memories
            .asSequence()
            .filter(::eligible)
            .sortedByDescending { memory ->
                val ageDays = max(0.0, (nowEpochMs - memory.updatedAtEpochMs) / 86_400_000.0)
                val recency = 1.0 / (1.0 + ageDays / 45.0)
                memory.importance.coerceIn(0f, 1f) * 0.75 + recency * 0.25
            }
            .take(limit.coerceAtLeast(1))
            .toList()

    private fun eligible(memory: MemoryRecord): Boolean =
        memory.kind == MemoryKind.FACT ||
            memory.kind == MemoryKind.PREFERENCE ||
            memory.kind == MemoryKind.GOAL

    internal fun tokens(text: String): Set<String> {
        val normalized = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return normalized
            .split(Regex("[^a-z0-9]+"))
            .asSequence()
            .filter { it.length >= 2 }
            .filterNot(STOP_WORDS::contains)
            .toSet()
    }

    private val STOP_WORDS = setOf(
        "a", "ao", "aos", "as", "com", "como", "da", "das", "de", "do", "dos",
        "e", "em", "eu", "foi", "me", "meu", "meus", "minha", "minhas", "na",
        "nas", "no", "nos", "o", "os", "para", "por", "que", "se", "um", "uma",
        "voce", "voces", "qual", "quais", "sobre", "isso", "isto",
    )
}
