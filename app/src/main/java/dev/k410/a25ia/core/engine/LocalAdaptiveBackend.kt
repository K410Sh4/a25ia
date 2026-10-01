package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.InferenceRequest
import dev.k410.a25ia.core.model.InferenceResult
import dev.k410.a25ia.core.model.UserIntent
import kotlin.math.roundToInt

/**
 * Local deterministic baseline used to validate orchestration, memory and adaptation.
 * It is intentionally not presented as a neural language model.
 */
class LocalAdaptiveBackend : InferenceBackend {
    override val id: String = "adaptive-local-v2"

    override suspend fun generate(request: InferenceRequest): InferenceResult {
        val start = System.nanoTime()
        val text = compose(request)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        return InferenceResult(
            text = text,
            backendId = id,
            latencyMs = elapsedMs,
            diagnostics = mapOf(
                "long_term_memory_hits" to request.memories.size.toString(),
                "session_turns" to request.sessionContext.size.toString(),
                "intent" to request.intent.name,
                "prompt_chars" to PromptAssembler.build(request).length.toString(),
                "backend" to id,
            ),
        )
    }

    private fun compose(request: InferenceRequest): String {
        val message = request.userMessage.trim()
        val settings = request.settings
        val p = settings.effectivePersonality()
        val facts = request.memories.map { it.text }.distinct()

        val opening = when {
            p.formality > 0.72f -> "Certo."
            p.empathy > 0.78f -> "Entendi."
            p.assertiveness > 0.78f -> "Certo."
            else -> ""
        }

        val core = when (request.intent) {
            UserIntent.IDENTITY ->
                "Eu sou ${settings.assistantName}, o núcleo adaptativo local do A25IA. " +
                    "Minha arquitetura possui memória de longo prazo separada da sessão, feedback isolado e personalidade configurável. " +
                    "O backend atual ainda não é um LLM neural completo."

            UserIntent.MEMORY ->
                if (facts.isEmpty()) {
                    "Ainda não tenho fatos ou preferências de longo prazo relevantes registrados sobre você."
                } else {
                    "Estas são as memórias de longo prazo que encontrei: " +
                        facts.take(settings.learning.memoryRetrievalCount).joinToString("; ") + "."
                }

            UserIntent.CONFIGURATION ->
                "Você pode alterar identidade, geração, personalidade, memória e aprendizado em Configurações. " +
                    "A personalidade base agora fica separada dos pequenos ajustes aprendidos por feedback."

            UserIntent.GREETING ->
                "Olá. Eu sou ${settings.assistantName}. A sessão atual e a memória de longo prazo estão separadas para evitar contexto contaminado."

            UserIntent.QUESTION ->
                answerOpenQuestion(message, request)

            UserIntent.STATEMENT ->
                acknowledge(message, request)
        }

        val humorSuffix = if (p.humor > 0.82f && core.length < 260) " 🙂" else ""
        val initiativeSuffix =
            if (p.initiative > 0.75f && request.intent !in setOf(UserIntent.IDENTITY, UserIntent.MEMORY, UserIntent.GREETING)) {
                " Seu feedback pode ajustar meu estilo sem alterar sua personalidade base."
            } else {
                ""
            }

        return listOf(opening, core + humorSuffix + initiativeSuffix)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    }

    private fun answerOpenQuestion(message: String, request: InferenceRequest): String {
        val p = request.settings.effectivePersonality()
        val memoryNote = if (request.memories.isEmpty()) {
            ""
        } else {
            " Encontrei ${request.memories.size} memória(s) de longo prazo realmente relacionada(s), mas não vou expor contexto interno sem necessidade."
        }

        val detail = if (p.verbosity >= 0.55f) {
            " Meu backend local determinístico ainda não possui conhecimento generativo amplo; " +
                "ele está servindo como baseline segura para o próximo backend LLM."
        } else {
            " Meu backend atual ainda não é um LLM, então não vou inventar a resposta."
        }

        return "Entendi a pergunta “${message.take(180)}”.$memoryNote$detail"
    }

    private fun acknowledge(message: String, request: InferenceRequest): String {
        val p = request.settings.effectivePersonality()
        val compact = message.replace(Regex("\\s+"), " ").take(180)
        val style = when {
            p.creativity > 0.80f -> "Vou considerar isso no contexto desta sessão"
            p.formality > 0.70f -> "A informação foi incorporada ao contexto recente da sessão"
            else -> "Vou manter isso no contexto desta sessão"
        }
        val caution = (p.skepticism * 100).roundToInt()
        val diagnostic = if (p.verbosity > 0.72f) " Cautela efetiva: $caution%." else ""
        return "$style: “$compact”.$diagnostic"
    }
}
