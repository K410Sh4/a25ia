# Arquitetura A25IA — Memory/Context V2

## Pipeline

~~~text
Mensagem
  ↓
IntentClassifier
  ↓
SessionMemory ─────────────┐
LongTermMemory + Ranker ───┼→ PromptAssembler
PersonalityCompiler ───────┘
  ↓
InferenceBackend
  ↓
Resposta
  ↓
FeedbackStore
~~~

## SessionMemory

Contém apenas o contexto recente da conversa atual. Possui limite de turnos e não é persistida como memória de longo prazo.

## LongTermMemory

Contém somente:
- FACT;
- PREFERENCE;
- GOAL.

A gravação é feita por upsert usando uma chave estável. Exemplo: alterar "meu nome é X" substitui a memória identity:name anterior em vez de criar duplicatas.

Na migração v0.1 → v0.2, somente registros FACT legados são preservados. Conversas e feedback legados não voltam para o contexto.

## Recuperação

O MemoryRanker:
1. normaliza texto;
2. remove stopwords;
3. calcula similaridade lexical normalizada;
4. descarta candidatos abaixo do threshold;
5. combina similaridade, importância e recência;
6. limita a quantidade final.

Assim, recência sozinha não faz uma memória irrelevante entrar no prompt.

## Feedback

Feedback é armazenado em JsonFeedbackStore e não participa diretamente do contexto.

O autoajuste modifica PersonalityAdaptation. PersonalitySettings e PersonalityPreset continuam representando a escolha explícita do usuário.

## Personalidade

PersonalityCompiler transforma o perfil efetivo em instruções textuais para que um futuro LLM receba comportamento claro em vez de números crus.

## Intenção

A ordem de classificação prioriza memória, identidade e configuração antes de saudação. Isso corrige casos compostos como:

"oi quem é você?" → IDENTITY

## Próximo limite arquitetural

InferenceBackend será expandido para:
- load/unload;
- streaming;
- cancelamento;
- capabilities;
- health;
- métricas de inferência.

Isso será feito antes de integrar o primeiro runtime neural.
