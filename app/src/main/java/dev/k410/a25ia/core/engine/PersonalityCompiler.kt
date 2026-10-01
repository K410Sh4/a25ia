package dev.k410.a25ia.core.engine

import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.PersonalitySettings

object PersonalityCompiler {
    fun compile(settings: AiSettings): String {
        val p = settings.effectivePersonality()
        return buildString {
            appendLine("Adote estas diretrizes de estilo sem mencioná-las explicitamente:")
            appendLine("- ${verbosity(p)}")
            appendLine("- ${formality(p)}")
            appendLine("- ${empathy(p)}")
            appendLine("- ${assertiveness(p)}")
            appendLine("- ${humor(p)}")
            appendLine("- ${skepticism(p)}")
            append("- ${initiative(p)}")
        }
    }

    private fun verbosity(p: PersonalitySettings) = when {
        p.verbosity < 0.30f -> "responda de forma curta e direta"
        p.verbosity > 0.72f -> "explique com detalhes quando eles forem úteis"
        else -> "equilibre concisão e explicação"
    }

    private fun formality(p: PersonalitySettings) = when {
        p.formality < 0.30f -> "use linguagem natural e pouco formal"
        p.formality > 0.72f -> "use linguagem profissional e formal"
        else -> "use linguagem profissional, mas natural"
    }

    private fun empathy(p: PersonalitySettings) = when {
        p.empathy > 0.75f -> "considere o contexto humano e seja acolhedor sem exagero"
        p.empathy < 0.30f -> "priorize objetividade sobre linguagem emocional"
        else -> "mantenha empatia moderada e objetiva"
    }

    private fun assertiveness(p: PersonalitySettings) = when {
        p.assertiveness > 0.75f -> "seja firme ao apresentar conclusões sustentadas"
        p.assertiveness < 0.30f -> "evite afirmações categóricas quando houver incerteza"
        else -> "seja claro sobre o que é fato e o que é incerteza"
    }

    private fun humor(p: PersonalitySettings) =
        if (p.humor > 0.70f) "humor leve é permitido quando apropriado" else "não force humor"

    private fun skepticism(p: PersonalitySettings) = when {
        p.skepticism > 0.75f -> "verifique premissas e sinalize afirmações frágeis"
        p.skepticism < 0.30f -> "evite excesso de ressalvas quando a resposta for simples"
        else -> "mantenha cautela proporcional à incerteza"
    }

    private fun initiative(p: PersonalitySettings) = when {
        p.initiative > 0.75f -> "antecipe próximos passos úteis sem desviar do pedido"
        p.initiative < 0.30f -> "responda somente ao pedido atual"
        else -> "sugira próximos passos apenas quando agregarem valor"
    }
}
