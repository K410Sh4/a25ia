package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.MemoryKind
import java.text.Normalizer

data class ExtractedMemory(
    val key: String,
    val kind: MemoryKind,
    val text: String,
    val importance: Float,
)

object FactExtractor {
    fun extract(text: String): List<ExtractedMemory> {
        val candidates = buildList {
            match(NAME, text)?.let { value ->
                add(
                    ExtractedMemory(
                        key = "identity:name",
                        kind = MemoryKind.FACT,
                        text = "Nome: $value",
                        importance = 1.0f,
                    ),
                )
            }
            match(LIKES, text)?.let { value ->
                add(
                    ExtractedMemory(
                        key = "preference:likes:${keyPart(value)}",
                        kind = MemoryKind.PREFERENCE,
                        text = "Gosta de: $value",
                        importance = 0.82f,
                    ),
                )
            }
            match(PREFERS, text)?.let { value ->
                add(
                    ExtractedMemory(
                        key = "preference:prefers:${keyPart(value)}",
                        kind = MemoryKind.PREFERENCE,
                        text = "Prefere: $value",
                        importance = 0.90f,
                    ),
                )
            }
            match(GOAL, text)?.let { value ->
                add(
                    ExtractedMemory(
                        key = "goal:${keyPart(value)}",
                        kind = MemoryKind.GOAL,
                        text = "Objetivo: $value",
                        importance = 0.95f,
                    ),
                )
            }
            match(LEARNING, text)?.let { value ->
                add(
                    ExtractedMemory(
                        key = "goal:learning:${keyPart(value)}",
                        kind = MemoryKind.GOAL,
                        text = "Está aprendendo: $value",
                        importance = 0.88f,
                    ),
                )
            }
        }

        return candidates.distinctBy { it.key }
    }

    private fun match(regex: Regex, text: String): String? =
        regex.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.trimSentence()
            ?.takeIf(String::isNotBlank)

    private fun String.trimSentence(): String =
        trim().trimEnd('.', '!', '?', ',', ';').take(180)

    private fun keyPart(value: String): String =
        Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(80)

    private val NAME = Regex("""(?i)\bmeu nome (?:é|e)\s+([^.!?\n]{2,80})""")
    private val LIKES = Regex("""(?i)\beu gosto de\s+([^.!?\n]{2,120})""")
    private val PREFERS = Regex("""(?i)\beu prefiro\s+([^.!?\n]{2,120})""")
    private val GOAL = Regex("""(?i)\bmeu objetivo (?:é|e)\s+([^.!?\n]{2,160})""")
    private val LEARNING = Regex("""(?i)\bestou aprendendo\s+([^.!?\n]{2,160})""")
}
