package dev.k410.a25ia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.k410.a25ia.AppContainer
import dev.k410.a25ia.core.engine.AiOrchestrator
import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.MemoryRecord
import dev.k410.a25ia.core.model.PersonalityPreset
import dev.k410.a25ia.data.SettingsRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessageUi(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val text: String,
    val backendId: String? = null,
    val feedback: FeedbackSignal? = null,
)

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM,
}

data class MainUiState(
    val settings: AiSettings = AiSettings(),
    val messages: List<ChatMessageUi> = listOf(
        ChatMessageUi(
            role = ChatRole.SYSTEM,
            text = "A25IA local iniciado. Memória, personalidade e aprendizado podem ser configurados.",
        ),
    ),
    val memories: List<MemoryRecord> = emptyList(),
    val isThinking: Boolean = false,
    val error: String? = null,
)

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val orchestrator: AiOrchestrator,
) : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        settingsRepository.settings
            .onEach { settings -> _state.update { it.copy(settings = settings) } }
            .launchIn(viewModelScope)
        refreshMemories()
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isBlank() || _state.value.isThinking) return

        val settings = _state.value.settings
        _state.update {
            it.copy(
                messages = it.messages + ChatMessageUi(role = ChatRole.USER, text = message),
                isThinking = true,
                error = null,
            )
        }

        viewModelScope.launch {
            runCatching { orchestrator.respond(message, settings) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            messages = it.messages + ChatMessageUi(
                                role = ChatRole.ASSISTANT,
                                text = result.text,
                                backendId = result.backendId,
                            ),
                            isThinking = false,
                        )
                    }
                    refreshMemories()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isThinking = false,
                            error = error.message ?: "Falha não identificada",
                            messages = it.messages + ChatMessageUi(
                                role = ChatRole.SYSTEM,
                                text = "Falha ao processar a mensagem. Nenhum resultado foi inventado.",
                            ),
                        )
                    }
                }
        }
    }

    fun updateSettings(transform: (AiSettings) -> AiSettings) {
        val updated = transform(_state.value.settings).normalized()
        _state.update { it.copy(settings = updated) }
        viewModelScope.launch {
            settingsRepository.save(updated)
        }
    }

    fun applyPreset(preset: PersonalityPreset) {
        updateSettings { preset.apply(it) }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.reset()
        }
    }

    fun feedback(messageId: String, signal: FeedbackSignal) {
        val message = _state.value.messages.firstOrNull { it.id == messageId } ?: return
        if (message.role != ChatRole.ASSISTANT) return

        viewModelScope.launch {
            val updated = orchestrator.applyFeedback(
                responseText = message.text,
                signal = signal,
                settings = _state.value.settings,
            )
            settingsRepository.save(updated)
            _state.update { state ->
                state.copy(
                    settings = updated,
                    messages = state.messages.map {
                        if (it.id == messageId) it.copy(feedback = signal) else it
                    },
                )
            }
            refreshMemories()
        }
    }

    fun refreshMemories() {
        viewModelScope.launch {
            runCatching { orchestrator.memories() }
                .onSuccess { memories -> _state.update { it.copy(memories = memories) } }
        }
    }

    fun clearMemory() {
        viewModelScope.launch {
            orchestrator.clearMemory()
            refreshMemories()
        }
    }

    class Factory(
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(
                settingsRepository = container.settingsRepository,
                orchestrator = container.orchestrator,
            ) as T
        }
    }
}
