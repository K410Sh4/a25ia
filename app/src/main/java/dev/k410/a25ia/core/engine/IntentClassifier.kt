package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.UserIntent
import java.text.Normalizer

object IntentClassifier {
    fun classify(text: String): UserIntent {
        val normalized = normalize(text)

        if (MEMORY_PATTERNS.any(normalized::contains)) return UserIntent.MEMORY
        if (IDENTITY_PATTERNS.any(normalized::contains)) return UserIntent.IDENTITY
        if (CONFIG_PATTERNS.any(normalized::contains)) return UserIntent.CONFIGURATION

        val words = normalized.split(Regex("\\s+")).filter(String::isNotBlank)
        if (
            GREETINGS.any { greeting ->
                normalized == greeting || (normalized.startsWith("$greeting ") && words.size <= 4)
            }
        ) {
            return UserIntent.GREETING
        }

        return if (text.trim().endsWith("?")) UserIntent.QUESTION else UserIntent.STATEMENT
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-z0-9 ]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private val MEMORY_PATTERNS = listOf(
        "o que voce lembra",
        "lembra de mim",
        "o que sabe sobre mim",
        "qual meu nome",
        "qual e meu nome",
        "minhas preferencias",
        "meus objetivos",
    )

    private val IDENTITY_PATTERNS = listOf(
        "quem e voce",
        "o que voce e",
        "quem voce e",
    )

    private val CONFIG_PATTERNS = listOf(
        "configuracao",
        "configuracoes",
        "personalidade",
        "parametros",
    )

    private val GREETINGS = listOf(
        "oi",
        "ola",
        "bom dia",
        "boa tarde",
        "boa noite",
    )
}
