package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.MemoryRole
import dev.k410.a25ia.core.model.SessionTurn

class SessionMemory {
    private val turns = ArrayDeque<SessionTurn>()

    @Synchronized
    fun append(role: MemoryRole, text: String, maxTurns: Int) {
        val clean = text.trim()
        if (clean.isBlank()) return

        turns.addLast(SessionTurn(role = role, text = clean))
        val bounded = maxTurns.coerceIn(2, 128)
        while (turns.size > bounded) {
            turns.removeFirst()
        }
    }

    @Synchronized
    fun snapshot(limit: Int): List<SessionTurn> =
        turns.takeLast(limit.coerceIn(1, 64))

    @Synchronized
    fun size(): Int = turns.size

    @Synchronized
    fun clear() {
        turns.clear()
    }
}
