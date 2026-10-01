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

        appendLine("[PERSONALIDADE]")
        appendLine(PersonalityCompiler.compile(request.settings))
        appendLine()

        if (request.memories.isNotEmpty()) {
            appendLine("[MEMÓRIA DE LONGO PRAZO RELEVANTE]")
            request.memories.forEach { memory ->
                appendLine("- ${memory.text}")
            }
            appendLine()
        }

        if (request.sessionContext.isNotEmpty()) {
            appendLine("[CONTEXTO RECENTE DA SESSÃO]")
            request.sessionContext.forEach { turn ->
                val role = when (turn.role) {
                    MemoryRole.USER -> "usuário"
                    MemoryRole.ASSISTANT -> "assistente"
                    MemoryRole.SYSTEM -> "sistema"
                }
                appendLine("- $role: ${turn.text}")
            }
            appendLine()
        }

        appendLine("[MENSAGEM ATUAL]")
        append(request.userMessage)
    }
}
