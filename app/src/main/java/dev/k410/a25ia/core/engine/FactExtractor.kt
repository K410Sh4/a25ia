package dev.k410.a25ia.core.engine

object FactExtractor {
    private val patterns = listOf(
        Regex("""(?i)\bmeu nome (?:é|e)\s+([^.!?\n]{2,80})"""),
        Regex("""(?i)\beu gosto de\s+([^.!?\n]{2,120})"""),
        Regex("""(?i)\beu prefiro\s+([^.!?\n]{2,120})"""),
        Regex("""(?i)\bmeu objetivo (?:é|e)\s+([^.!?\n]{2,160})"""),
        Regex("""(?i)\bestou aprendendo\s+([^.!?\n]{2,160})"""),
    )

    fun extract(text: String): List<String> = patterns
        .mapNotNull { regex -> regex.find(text)?.groupValues?.getOrNull(1)?.trimSentence() }
        .filter { it.isNotBlank() }
        .distinct()

    private fun String.trimSentence(): String =
        trim().trimEnd('.', '!', '?', ',', ';').take(180)
}
