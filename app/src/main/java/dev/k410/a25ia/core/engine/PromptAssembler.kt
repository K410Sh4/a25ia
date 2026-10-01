package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.InferenceRequest
import dev.k410.a25ia.core.model.MemoryRole

object PromptAssembler {
    fun build(request: InferenceRequest): String = buildString {
        appendLine("[IDENTIDADE]")
        appendLine("Nome: ${request.settings.assistantName}")
        appendLine("Idioma: ${request.settings.responseLanguage}")
        appendLine(request.settings.systemInstruction)
        appendLine()

        if (request.memories.isNotEmpty()) {
            appendLine("[MEMÓRIA RELEVANTE]")
            request.memories.forEach { memory ->
                val role = when (memory.role) {
                    MemoryRole.USER -> "usuário"
                    MemoryRole.ASSISTANT -> "assistente"
                    MemoryRole.SYSTEM -> "sistema"
                }
                appendLine("- $role: ${memory.text}")
            }
            appendLine()
        }

        appendLine("[MENSAGEM ATUAL]")
        append(request.userMessage)
    }
}
