package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.InferenceRequest
import dev.k410.a25ia.core.model.InferenceResult
import dev.k410.a25ia.core.model.MemoryKind
import dev.k410.a25ia.core.model.MemoryRole
import kotlin.math.roundToInt

/**
 * Baseline local cognitive backend.
 *
 * It is intentionally honest: this is not an embedded LLM. It provides a fully local,
 * deterministic adaptive baseline with memory, intent routing and configurable style.
 * A model-backed implementation can replace it through [InferenceBackend].
 */
class LocalAdaptiveBackend : InferenceBackend {
    override val id: String = "adaptive-local-v1"

    override suspend fun generate(request: InferenceRequest): InferenceResult {
        val start = System.nanoTime()
        val text = compose(request)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        return InferenceResult(
            text = text,
            backendId = id,
            latencyMs = elapsedMs,
            diagnostics = mapOf(
                "memory_hits" to request.memories.size.toString(),
                "prompt_chars" to PromptAssembler.build(request).length.toString(),
                "backend" to id,
            ),
        )
    }

    private fun compose(request: InferenceRequest): String {
        val message = request.userMessage.trim()
        val lower = message.lowercase()
        val settings = request.settings
        val p = settings.personality

        val facts = request.memories
            .filter { it.kind == MemoryKind.FACT && it.role == MemoryRole.USER }
            .map { it.text }
            .distinct()

        val greeting = listOf("oi", "olá", "ola", "bom dia", "boa tarde", "boa noite")
            .any { lower == it || lower.startsWith("$it ") }

        val asksMemory = listOf(
            "o que você lembra",
            "o que voce lembra",
            "lembra de mim",
            "o que sabe sobre mim",
            "qual meu nome",
        ).any(lower::contains)

        val asksIdentity = listOf("quem é você", "quem e voce", "o que você é", "o que voce e")
            .any(lower::contains)

        val opening = when {
            p.formality > 0.72f -> "Certo."
            p.empathy > 0.78f -> "Entendi."
            p.assertiveness > 0.78f -> "Certo."
            else -> ""
        }

        val core = when {
            greeting ->
                "Olá. Eu sou ${settings.assistantName}. Minha memória e meu perfil adaptativo estão ativos conforme suas configurações."

            asksIdentity ->
                "Eu sou ${settings.assistantName}, o núcleo adaptativo local do A25IA. " +
                    "Nesta versão eu possuo memória persistente, recuperação de contexto, feedback, " +
                    "personalidade configurável e autoajuste limitado. Meu backend atual ainda não é um LLM completo."

            asksMemory && facts.isNotEmpty() ->
                "Estas são as memórias factuais mais relevantes que encontrei: " +
                    facts.take(settings.learning.memoryRetrievalCount).joinToString("; ") + "."

            asksMemory ->
                "Ainda não encontrei memórias factuais suficientes sobre você. " +
                    "Frases como “meu nome é...”, “eu gosto de...” ou “meu objetivo é...” podem ser registradas localmente."

            lower.contains("configura") || lower.contains("personalidade") ->
                "Você pode alterar nome, instrução do sistema, geração, personalidade, memória e aprendizado na aba Configurações. " +
                    "Os valores são normalizados antes de serem persistidos."

            message.endsWith("?") ->
                answerOpenQuestion(message, request)

            else ->
                acknowledge(message, request)
        }

        val humorSuffix = if (p.humor > 0.82f && core.length < 260) " 🙂" else ""
        val initiativeSuffix = if (p.initiative > 0.75f && !asksMemory && !asksIdentity && !greeting) {
            " Posso usar seu feedback para ajustar meu estilo dentro dos limites configurados."
        } else {
            ""
        }

        return listOf(opening, core + humorSuffix + initiativeSuffix)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
    }

    private fun answerOpenQuestion(message: String, request: InferenceRequest): String {
        val p = request.settings.personality
        val related = request.memories
            .filter { it.role == MemoryRole.USER }
            .take(3)

        val context = if (related.isEmpty()) {
            ""
        } else {
            " Contexto recuperado: " + related.joinToString(" | ") { it.text.take(100) } + "."
        }

        val detail = if (p.verbosity >= 0.55f) {
            " Eu consigo organizar contexto e memória localmente, mas ainda não devo fingir conhecimento generativo amplo: " +
                "o próximo backend precisa ser um modelo local real e será comparado por latência, RAM, estabilidade e qualidade."
        } else {
            " Meu backend local atual é deliberadamente limitado; não vou inventar uma resposta que ele não consegue sustentar."
        }

        return "Entendi a pergunta “${message.take(180)}”.$context$detail"
    }

    private fun acknowledge(message: String, request: InferenceRequest): String {
        val p = request.settings.personality
        val compact = message.replace(Regex("\\s+"), " ").take(180)
        val style = when {
            p.creativity > 0.80f -> "Vou incorporar isso ao contexto de forma flexível"
            p.formality > 0.70f -> "A informação foi incorporada ao contexto disponível"
            else -> "Vou manter isso no contexto"
        }
        val confidence = (request.settings.personality.skepticism * 100).roundToInt()
        val diagnostic = if (p.verbosity > 0.72f) " Perfil de cautela atual: $confidence%." else ""
        return "$style: “$compact”.$diagnostic"
    }
}
