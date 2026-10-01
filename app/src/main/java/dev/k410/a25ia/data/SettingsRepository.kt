package dev.k410.a25ia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.GenerationSettings
import dev.k410.a25ia.core.model.LearningSettings
import dev.k410.a25ia.core.model.PersonalityAdaptation
import dev.k410.a25ia.core.model.PersonalityPreset
import dev.k410.a25ia.core.model.PersonalitySettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.aiSettingsDataStore: DataStore<Preferences> by preferencesDataStore("a25ia_settings")

class SettingsRepository(private val context: Context) {
    val settings: Flow<AiSettings> = context.aiSettingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map(::fromPreferences)

    suspend fun save(value: AiSettings) {
        val settings = value.normalized()
        context.aiSettingsDataStore.edit { p ->
            p[Keys.assistantName] = settings.assistantName
            p[Keys.preset] = settings.personalityPreset.name
            p[Keys.systemInstruction] = settings.systemInstruction
            p[Keys.responseLanguage] = settings.responseLanguage

            p[Keys.temperature] = settings.generation.temperature
            p[Keys.topP] = settings.generation.topP
            p[Keys.topK] = settings.generation.topK
            p[Keys.maxOutputTokens] = settings.generation.maxOutputTokens
            p[Keys.contextMessages] = settings.generation.contextMessages
            p[Keys.repetitionPenalty] = settings.generation.repetitionPenalty
            p[Keys.presencePenalty] = settings.generation.presencePenalty
            p[Keys.frequencyPenalty] = settings.generation.frequencyPenalty
            p[Keys.seed] = settings.generation.seed

            p[Keys.creativity] = settings.personality.creativity
            p[Keys.verbosity] = settings.personality.verbosity
            p[Keys.empathy] = settings.personality.empathy
            p[Keys.assertiveness] = settings.personality.assertiveness
            p[Keys.humor] = settings.personality.humor
            p[Keys.formality] = settings.personality.formality
            p[Keys.curiosity] = settings.personality.curiosity
            p[Keys.skepticism] = settings.personality.skepticism
            p[Keys.initiative] = settings.personality.initiative

            p[Keys.adaptVerbosity] = settings.adaptation.verbosityOffset
            p[Keys.adaptEmpathy] = settings.adaptation.empathyOffset
            p[Keys.adaptSkepticism] = settings.adaptation.skepticismOffset
            p[Keys.adaptInitiative] = settings.adaptation.initiativeOffset

            p[Keys.memoryEnabled] = settings.learning.memoryEnabled
            p[Keys.autoStoreFacts] = settings.learning.autoStoreFacts
            p[Keys.learningEnabled] = settings.learning.learningEnabled
            p[Keys.safeAutoTune] = settings.learning.safeAutoTune
            p[Keys.learningRate] = settings.learning.learningRate
            p[Keys.maxAdjustment] = settings.learning.maxAdjustmentPerFeedback
            p[Keys.memoryRetrievalCount] = settings.learning.memoryRetrievalCount
            p[Keys.memoryLimit] = settings.learning.memoryLimit
        }
    }

    suspend fun reset() {
        context.aiSettingsDataStore.edit { it.clear() }
    }

    private fun fromPreferences(p: Preferences): AiSettings {
        val defaults = AiSettings()
        val generation = defaults.generation
        val personality = defaults.personality
        val adaptation = defaults.adaptation
        val learning = defaults.learning

        return AiSettings(
            assistantName = p[Keys.assistantName] ?: defaults.assistantName,
            personalityPreset = p[Keys.preset]
                ?.let { runCatching { PersonalityPreset.valueOf(it) }.getOrNull() }
                ?: defaults.personalityPreset,
            systemInstruction = p[Keys.systemInstruction] ?: defaults.systemInstruction,
            responseLanguage = p[Keys.responseLanguage] ?: defaults.responseLanguage,
            generation = GenerationSettings(
                temperature = p[Keys.temperature] ?: generation.temperature,
                topP = p[Keys.topP] ?: generation.topP,
                topK = p[Keys.topK] ?: generation.topK,
                maxOutputTokens = p[Keys.maxOutputTokens] ?: generation.maxOutputTokens,
                contextMessages = p[Keys.contextMessages] ?: generation.contextMessages,
                repetitionPenalty = p[Keys.repetitionPenalty] ?: generation.repetitionPenalty,
                presencePenalty = p[Keys.presencePenalty] ?: generation.presencePenalty,
                frequencyPenalty = p[Keys.frequencyPenalty] ?: generation.frequencyPenalty,
                seed = p[Keys.seed] ?: generation.seed,
            ),
            personality = PersonalitySettings(
                creativity = p[Keys.creativity] ?: personality.creativity,
                verbosity = p[Keys.verbosity] ?: personality.verbosity,
                empathy = p[Keys.empathy] ?: personality.empathy,
                assertiveness = p[Keys.assertiveness] ?: personality.assertiveness,
                humor = p[Keys.humor] ?: personality.humor,
                formality = p[Keys.formality] ?: personality.formality,
                curiosity = p[Keys.curiosity] ?: personality.curiosity,
                skepticism = p[Keys.skepticism] ?: personality.skepticism,
                initiative = p[Keys.initiative] ?: personality.initiative,
            ),
            adaptation = PersonalityAdaptation(
                verbosityOffset = p[Keys.adaptVerbosity] ?: adaptation.verbosityOffset,
                empathyOffset = p[Keys.adaptEmpathy] ?: adaptation.empathyOffset,
                skepticismOffset = p[Keys.adaptSkepticism] ?: adaptation.skepticismOffset,
                initiativeOffset = p[Keys.adaptInitiative] ?: adaptation.initiativeOffset,
            ),
            learning = LearningSettings(
                memoryEnabled = p[Keys.memoryEnabled] ?: learning.memoryEnabled,
                autoStoreFacts = p[Keys.autoStoreFacts] ?: learning.autoStoreFacts,
                learningEnabled = p[Keys.learningEnabled] ?: learning.learningEnabled,
                safeAutoTune = p[Keys.safeAutoTune] ?: learning.safeAutoTune,
                learningRate = p[Keys.learningRate] ?: learning.learningRate,
                maxAdjustmentPerFeedback = p[Keys.maxAdjustment] ?: learning.maxAdjustmentPerFeedback,
                memoryRetrievalCount = p[Keys.memoryRetrievalCount] ?: learning.memoryRetrievalCount,
                memoryLimit = p[Keys.memoryLimit] ?: learning.memoryLimit,
            ),
        ).normalized()
    }

    private object Keys {
        val assistantName = stringPreferencesKey("assistant_name")
        val preset = stringPreferencesKey("personality_preset")
        val systemInstruction = stringPreferencesKey("system_instruction")
        val responseLanguage = stringPreferencesKey("response_language")

        val temperature = floatPreferencesKey("temperature")
        val topP = floatPreferencesKey("top_p")
        val topK = intPreferencesKey("top_k")
        val maxOutputTokens = intPreferencesKey("max_output_tokens")
        val contextMessages = intPreferencesKey("context_messages")
        val repetitionPenalty = floatPreferencesKey("repetition_penalty")
        val presencePenalty = floatPreferencesKey("presence_penalty")
        val frequencyPenalty = floatPreferencesKey("frequency_penalty")
        val seed = intPreferencesKey("seed")

        val creativity = floatPreferencesKey("creativity")
        val verbosity = floatPreferencesKey("verbosity")
        val empathy = floatPreferencesKey("empathy")
        val assertiveness = floatPreferencesKey("assertiveness")
        val humor = floatPreferencesKey("humor")
        val formality = floatPreferencesKey("formality")
        val curiosity = floatPreferencesKey("curiosity")
        val skepticism = floatPreferencesKey("skepticism")
        val initiative = floatPreferencesKey("initiative")

        val adaptVerbosity = floatPreferencesKey("adapt_verbosity")
        val adaptEmpathy = floatPreferencesKey("adapt_empathy")
        val adaptSkepticism = floatPreferencesKey("adapt_skepticism")
        val adaptInitiative = floatPreferencesKey("adapt_initiative")

        val memoryEnabled = booleanPreferencesKey("memory_enabled")
        val autoStoreFacts = booleanPreferencesKey("auto_store_facts")
        val learningEnabled = booleanPreferencesKey("learning_enabled")
        val safeAutoTune = booleanPreferencesKey("safe_auto_tune")
        val learningRate = floatPreferencesKey("learning_rate")
        val maxAdjustment = floatPreferencesKey("max_adjustment")
        val memoryRetrievalCount = intPreferencesKey("memory_retrieval_count")
        val memoryLimit = intPreferencesKey("memory_limit")
    }
}
