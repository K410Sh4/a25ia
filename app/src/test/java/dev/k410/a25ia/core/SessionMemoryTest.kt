package dev.k410.a25ia.core

import dev.k410.a25ia.core.engine.SessionMemory
import dev.k410.a25ia.core.model.MemoryRole
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionMemoryTest {
    @Test
    fun sessionIsBounded() {
        val memory = SessionMemory()

        memory.append(MemoryRole.USER, "um", maxTurns = 2)
        memory.append(MemoryRole.ASSISTANT, "dois", maxTurns = 2)
        memory.append(MemoryRole.USER, "três", maxTurns = 2)

        val snapshot = memory.snapshot(10)
        assertEquals(2, snapshot.size)
        assertEquals("dois", snapshot[0].text)
        assertEquals("três", snapshot[1].text)
    }
}
