package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.MemoryRanker
import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRecord
import dev.k410.a25ia.core.model.MemoryRole
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryRankerTest {
    @Test
    fun relevantMemoryRanksBeforeUnrelatedMemory() {
        val now = 1_800_000_000_000L
        val memories = listOf(
            MemoryRecord(
                id = "1",
                createdAtEpochMs = now - 5_000,
                kind = MemoryKind.FACT,
                role = MemoryRole.USER,
                text = "Python é meu foco de estudo",
                importance = 0.8f,
            ),
            MemoryRecord(
                id = "2",
                createdAtEpochMs = now - 1_000,
                kind = MemoryKind.FACT,
                role = MemoryRole.USER,
                text = "Gosto de música",
                importance = 0.8f,
            ),
        )

        val ranked = MemoryRanker.rank(
            query = "qual é meu foco em Python?",
            memories = memories,
            limit = 2,
            nowEpochMs = now,
        )

        assertEquals("1", ranked.first().id)
    }
}
