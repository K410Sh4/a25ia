package dev.k410.a25ia.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.k410.a25ia.core.model.AiSettings
import dev.k410.a25ia.core.model.FeedbackSignal
import dev.k410.a25ia.core.model.MemoryRecord
import dev.k410.a25ia.core.model.PersonalityPreset
import dev.k410.a25ia.core.model.percentLabel
import java.util.Locale
import kotlin.math.roundToInt

private enum class AppTab(val label: String) {
    CHAT("Chat"),
    SETTINGS("Configurações"),
    MEMORY("Memória"),
    ABOUT("Status"),
}

@Composable
fun A25IaApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(AppTab.CHAT) }
    val scheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    AppTab.entries.forEach { item ->
                        TextButton(onClick = { tab = item }) {
                            Text(
                                text = item.label,
                                fontWeight = if (tab == item) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when (tab) {
                    AppTab.CHAT -> ChatScreen(state, viewModel)
                    AppTab.SETTINGS -> SettingsScreen(
                        settings = state.settings,
                        onUpdate = viewModel::updateSettings,
                        onPreset = viewModel::applyPreset,
                        onReset = viewModel::resetSettings,
                    )
                    AppTab.MEMORY -> MemoryScreen(
                        memories = state.memories,
                        onRefresh = viewModel::refreshMemories,
                        onClear = viewModel::clearMemory,
                    )
                    AppTab.ABOUT -> StatusScreen(state)
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(state: MainUiState, viewModel: MainViewModel) {
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(12.dp),
    ) {
        Text(
            text = state.settings.assistantName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Backend local • ${state.settings.personalityPreset.label}",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageCard(
                    message = message,
                    onFeedback = { signal -> viewModel.feedback(message.id, signal) },
                )
            }
            if (state.isThinking) {
                item {
                    Text("Processando localmente…", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                label = { Text("Mensagem") },
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.send(input)
                        input = ""
                    },
                ),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = input.isNotBlank() && !state.isThinking,
                onClick = {
                    viewModel.send(input)
                    input = ""
                },
            ) {
                Text("Enviar")
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: ChatMessageUi,
    onFeedback: (FeedbackSignal) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = when (message.role) {
                    ChatRole.USER -> "Você"
                    ChatRole.ASSISTANT -> "A25IA"
                    ChatRole.SYSTEM -> "Sistema"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(message.text)

            if (message.role == ChatRole.ASSISTANT) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { onFeedback(FeedbackSignal.POSITIVE) },
                        enabled = message.feedback == null,
                    ) { Text("👍") }
                    Spacer(Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { onFeedback(FeedbackSignal.NEGATIVE) },
                        enabled = message.feedback == null,
                    ) { Text("👎") }
                    message.feedback?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (it == FeedbackSignal.POSITIVE) "Feedback positivo aplicado"
                            else "Feedback negativo aplicado",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                message.backendId?.let {
                    Text("backend: $it", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AiSettings,
    onUpdate: ((AiSettings) -> AiSettings) -> Unit,
    onPreset: (PersonalityPreset) -> Unit,
    onReset: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Text("Configurações completas", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Tudo abaixo é persistido localmente. Parâmetros de geração serão reutilizados por backends de modelo compatíveis.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        item {
            SettingsCard("Identidade") {
                TextSetting(
                    label = "Nome da IA",
                    value = settings.assistantName,
                    onValue = { value -> onUpdate { it.copy(assistantName = value) } },
                )
                TextSetting(
                    label = "Idioma de resposta",
                    value = settings.responseLanguage,
                    onValue = { value -> onUpdate { it.copy(responseLanguage = value) } },
                )
                OutlinedTextField(
                    value = settings.systemInstruction,
                    onValueChange = { value -> onUpdate { it.copy(systemInstruction = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Instrução do sistema") },
                    minLines = 3,
                    maxLines = 8,
                )
            }
        }

        item {
            SettingsCard("Personalidade") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(PersonalityPreset.entries) { preset ->
                        FilterChip(
                            selected = settings.personalityPreset == preset,
                            onClick = { onPreset(preset) },
                            label = { Text(preset.label) },
                        )
                    }
                }
                FloatSetting("Criatividade", settings.personality.creativity) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(creativity = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Verbosidade", settings.personality.verbosity) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(verbosity = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Empatia", settings.personality.empathy) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(empathy = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Assertividade", settings.personality.assertiveness) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(assertiveness = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Humor", settings.personality.humor) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(humor = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Formalidade", settings.personality.formality) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(formality = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Curiosidade", settings.personality.curiosity) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(curiosity = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Ceticismo", settings.personality.skepticism) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(skepticism = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
                FloatSetting("Iniciativa", settings.personality.initiative) {
                    onUpdate { s -> s.copy(personality = s.personality.copy(initiative = it), personalityPreset = PersonalityPreset.CUSTOM) }
                }
            }
        }

        item {
            SettingsCard("Geração") {
                RangeSetting("Temperatura", settings.generation.temperature, 0f..2f) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(temperature = it)) }
                }
                RangeSetting("Top-P", settings.generation.topP, 0.05f..1f) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(topP = it)) }
                }
                IntSliderSetting("Top-K", settings.generation.topK, 1..200) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(topK = it)) }
                }
                IntSliderSetting("Máx. tokens", settings.generation.maxOutputTokens, 32..4096) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(maxOutputTokens = it)) }
                }
                IntSliderSetting("Mensagens de contexto", settings.generation.contextMessages, 1..64) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(contextMessages = it)) }
                }
                RangeSetting("Repetition penalty", settings.generation.repetitionPenalty, 0.5f..2f) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(repetitionPenalty = it)) }
                }
                RangeSetting("Presence penalty", settings.generation.presencePenalty, -2f..2f) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(presencePenalty = it)) }
                }
                RangeSetting("Frequency penalty", settings.generation.frequencyPenalty, -2f..2f) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(frequencyPenalty = it)) }
                }
                IntTextSetting("Seed", settings.generation.seed) {
                    onUpdate { s -> s.copy(generation = s.generation.copy(seed = it)) }
                }
            }
        }

        item {
            SettingsCard("Memória e aprendizado") {
                ToggleSetting("Memória persistente", settings.learning.memoryEnabled) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(memoryEnabled = it)) }
                }
                ToggleSetting("Extrair fatos automaticamente", settings.learning.autoStoreFacts) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(autoStoreFacts = it)) }
                }
                ToggleSetting("Aprendizado por feedback", settings.learning.learningEnabled) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(learningEnabled = it)) }
                }
                ToggleSetting("Autoajuste seguro", settings.learning.safeAutoTune) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(safeAutoTune = it)) }
                }
                RangeSetting("Taxa de aprendizado", settings.learning.learningRate, 0f..0.5f) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(learningRate = it)) }
                }
                RangeSetting("Ajuste máximo por feedback", settings.learning.maxAdjustmentPerFeedback, 0f..0.2f) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(maxAdjustmentPerFeedback = it)) }
                }
                IntSliderSetting("Memórias recuperadas", settings.learning.memoryRetrievalCount, 1..20) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(memoryRetrievalCount = it)) }
                }
                IntSliderSetting("Limite de memórias", settings.learning.memoryLimit, 50..5000) {
                    onUpdate { s -> s.copy(learning = s.learning.copy(memoryLimit = it)) }
                }
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onReset,
            ) {
                Text("Restaurar configurações padrão")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MemoryScreen(
    memories: List<MemoryRecord>,
    onRefresh: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Memória local", style = MaterialTheme.typography.headlineSmall)
                Text("${memories.size} registros", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onRefresh) { Text("Atualizar") }
            TextButton(onClick = onClear, enabled = memories.isNotEmpty()) { Text("Limpar") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(memories, key = { it.id }) { memory ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            "${memory.kind.name} • ${memory.role.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(memory.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusScreen(state: MainUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("A25IA v0.1", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            StatusCard("Backend", "adaptive-local-v1")
            StatusCard("Execução", "100% local nesta baseline; sem API key e sem rede")
            StatusCard("Memória", if (state.settings.learning.memoryEnabled) "Ativa" else "Desativada")
            StatusCard("Aprendizado", if (state.settings.learning.learningEnabled) "Feedback adaptativo ativo" else "Desativado")
            StatusCard("Registros", state.memories.size.toString())
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Limitação conhecida", fontWeight = FontWeight.Bold)
                    Text(
                        "O backend inicial é um núcleo cognitivo determinístico, não um LLM. " +
                            "Ele existe como baseline verificável para memória, adaptação, configuração e UI. " +
                            "Um backend neural local será conectado pela interface InferenceBackend depois de benchmark real no aparelho.",
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, value: String) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(value)
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun TextSetting(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
    )
}

@Composable
private fun FloatSetting(label: String, value: Float, onValue: (Float) -> Unit) {
    Text("$label: ${value.percentLabel()}")
    Slider(value = value, onValueChange = onValue, valueRange = 0f..1f)
}

@Composable
private fun RangeSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValue: (Float) -> Unit,
) {
    Text("$label: ${String.format(Locale.ROOT, "%.2f", value)}")
    Slider(value = value, onValueChange = onValue, valueRange = range)
}

@Composable
private fun IntSliderSetting(
    label: String,
    value: Int,
    range: IntRange,
    onValue: (Int) -> Unit,
) {
    Text("$label: $value")
    Slider(
        value = value.toFloat(),
        onValueChange = { onValue(it.roundToInt().coerceIn(range.first, range.last)) },
        valueRange = range.first.toFloat()..range.last.toFloat(),
    )
}

@Composable
private fun IntTextSetting(label: String, value: Int, onValue: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { raw -> raw.toIntOrNull()?.let(onValue) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun ToggleSetting(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onChecked)
    }
}
