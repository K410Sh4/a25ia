package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.MemoryRanker
import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryRankerTest {
    private val now = 1_800_000_000_000L

    @Test
    fun relevantMemoryRanksBeforeUnrelatedMemory() {
        val memories = listOf(
            memory("1", "focus:python", "Python é meu foco de estudo", now - 5_000),
            memory("2", "likes:music", "Gosta de: música", now - 1_000),
        )

        val ranked = MemoryRanker.rank(
            query = "qual é meu foco em Python?",
            memories = memories,
            limit = 2,
            nowEpochMs = now,
        )

        assertEquals("1", ranked.first().id)
    }

    @Test
    fun unrelatedMemoryIsDiscardedInsteadOfInjectedIntoContext() {
        val memories = listOf(
            memory("1", "topic:water", "Água é um assunto da conversa anterior", now - 1_000),
            memory("2", "topic:clouds", "Nuvens foram perguntadas anteriormente", now - 500),
        )

        val ranked = MemoryRanker.rank(
            query = "o que é o sol?",
            memories = memories,
            limit = 5,
            nowEpochMs = now,
        )

        assertTrue(ranked.isEmpty())
    }

    private fun memory(
        id: String,
        key: String,
        text: String,
        updated: Long,
    ) = MemoryRecord(
        id = id,
        key = key,
        createdAtEpochMs = updated,
        updatedAtEpochMs = updated,
        kind = MemoryKind.FACT,
        text = text,
        importance = 0.8f,
    )
}
