package dev.k410.a25ia.core.model

import kotlin.math.roundToInt

data class GenerationSettings(
    val temperature: Float = 0.70f,
    val topP: Float = 0.90f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 512,
    val contextMessages: Int = 12,
    val repetitionPenalty: Float = 1.05f,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f,
    val seed: Int = 41025,
) {
    fun normalized() = copy(
        temperature = temperature.coerceIn(0f, 2f),
        topP = topP.coerceIn(0.05f, 1f),
        topK = topK.coerceIn(1, 200),
        maxOutputTokens = maxOutputTokens.coerceIn(32, 4096),
        contextMessages = contextMessages.coerceIn(1, 64),
        repetitionPenalty = repetitionPenalty.coerceIn(0.5f, 2f),
        presencePenalty = presencePenalty.coerceIn(-2f, 2f),
        frequencyPenalty = frequencyPenalty.coerceIn(-2f, 2f),
    )
}

data class PersonalitySettings(
    val creativity: Float = 0.55f,
    val verbosity: Float = 0.45f,
    val empathy: Float = 0.55f,
    val assertiveness: Float = 0.55f,
    val humor: Float = 0.20f,
    val formality: Float = 0.45f,
    val curiosity: Float = 0.70f,
    val skepticism: Float = 0.65f,
    val initiative: Float = 0.60f,
) {
    fun normalized() = copy(
        creativity = creativity.unit(),
        verbosity = verbosity.unit(),
        empathy = empathy.unit(),
        assertiveness = assertiveness.unit(),
        humor = humor.unit(),
        formality = formality.unit(),
        curiosity = curiosity.unit(),
        skepticism = skepticism.unit(),
        initiative = initiative.unit(),
    )

    private fun Float.unit() = coerceIn(0f, 1f)
}

data class PersonalityAdaptation(
    val verbosityOffset: Float = 0f,
    val empathyOffset: Float = 0f,
    val skepticismOffset: Float = 0f,
    val initiativeOffset: Float = 0f,
) {
    fun normalized() = copy(
        verbosityOffset = verbosityOffset.coerceIn(-0.25f, 0.25f),
        empathyOffset = empathyOffset.coerceIn(-0.25f, 0.25f),
        skepticismOffset = skepticismOffset.coerceIn(-0.25f, 0.25f),
        initiativeOffset = initiativeOffset.coerceIn(-0.25f, 0.25f),
    )

    val isNeutral: Boolean
        get() = verbosityOffset == 0f &&
            empathyOffset == 0f &&
            skepticismOffset == 0f &&
            initiativeOffset == 0f
}

data class LearningSettings(
    val memoryEnabled: Boolean = true,
    val autoStoreFacts: Boolean = true,
    val learningEnabled: Boolean = true,
    val safeAutoTune: Boolean = true,
    val learningRate: Float = 0.08f,
    val maxAdjustmentPerFeedback: Float = 0.05f,
    val memoryRetrievalCount: Int = 6,
    val memoryLimit: Int = 500,
) {
    fun normalized() = copy(
        learningRate = learningRate.coerceIn(0f, 0.5f),
        maxAdjustmentPerFeedback = maxAdjustmentPerFeedback.coerceIn(0f, 0.20f),
        memoryRetrievalCount = memoryRetrievalCount.coerceIn(1, 20),
        memoryLimit = memoryLimit.coerceIn(50, 5000),
    )
}

data class AiSettings(
    val assistantName: String = "A25IA",
    val personalityPreset: PersonalityPreset = PersonalityPreset.BALANCED,
    val systemInstruction: String = DEFAULT_SYSTEM_INSTRUCTION,
    val responseLanguage: String = "pt-BR",
    val generation: GenerationSettings = GenerationSettings(),
    val personality: PersonalitySettings = PersonalitySettings(),
    val adaptation: PersonalityAdaptation = PersonalityAdaptation(),
    val learning: LearningSettings = LearningSettings(),
) {
    fun normalized() = copy(
        assistantName = assistantName.trim().ifBlank { "A25IA" }.take(32),
        systemInstruction = systemInstruction.trim().take(8_000),
        responseLanguage = responseLanguage.trim().ifBlank { "pt-BR" }.take(16),
        generation = generation.normalized(),
        personality = personality.normalized(),
        adaptation = adaptation.normalized(),
        learning = learning.normalized(),
    )

    fun effectivePersonality(): PersonalitySettings {
        val base = personality.normalized()
        val learned = adaptation.normalized()
        return base.copy(
            verbosity = (base.verbosity + learned.verbosityOffset).coerceIn(0f, 1f),
            empathy = (base.empathy + learned.empathyOffset).coerceIn(0f, 1f),
            skepticism = (base.skepticism + learned.skepticismOffset).coerceIn(0f, 1f),
            initiative = (base.initiative + learned.initiativeOffset).coerceIn(0f, 1f),
        )
    }

    companion object {
        const val DEFAULT_SYSTEM_INSTRUCTION =
            "Seja útil, claro, verificável e honesto sobre limitações. " +
                "Use memória apenas quando relevante. Não invente capacidades nem resultados."
    }
}

enum class PersonalityPreset(val label: String) {
    BALANCED("Equilibrada"),
    TECHNICAL("Técnica"),
    CREATIVE("Criativa"),
    DIRECT("Direta"),
    FRIENDLY("Amigável"),
    CUSTOM("Personalizada");

    fun apply(base: AiSettings): AiSettings {
        val profile = when (this) {
            BALANCED -> PersonalitySettings()
            TECHNICAL -> PersonalitySettings(
                creativity = 0.30f,
                verbosity = 0.68f,
                empathy = 0.42f,
                assertiveness = 0.62f,
                humor = 0.05f,
                formality = 0.70f,
                curiosity = 0.72f,
                skepticism = 0.88f,
                initiative = 0.62f,
            )
            CREATIVE -> PersonalitySettings(
                creativity = 0.92f,
                verbosity = 0.60f,
                empathy = 0.62f,
                assertiveness = 0.48f,
                humor = 0.58f,
                formality = 0.25f,
                curiosity = 0.90f,
                skepticism = 0.48f,
                initiative = 0.78f,
            )
            DIRECT -> PersonalitySettings(
                creativity = 0.28f,
                verbosity = 0.20f,
                empathy = 0.38f,
                assertiveness = 0.88f,
                humor = 0.06f,
                formality = 0.42f,
                curiosity = 0.42f,
                skepticism = 0.78f,
                initiative = 0.72f,
            )
            FRIENDLY -> PersonalitySettings(
                creativity = 0.62f,
                verbosity = 0.48f,
                empathy = 0.90f,
                assertiveness = 0.42f,
                humor = 0.42f,
                formality = 0.18f,
                curiosity = 0.78f,
                skepticism = 0.52f,
                initiative = 0.66f,
            )
            CUSTOM -> base.personality
        }
        return base.copy(
            personalityPreset = this,
            personality = profile,
            adaptation = PersonalityAdaptation(),
        )
    }
}

fun Float.percentLabel(): String = "${(coerceIn(0f, 1f) * 100).roundToInt()}%"
