# A25IA

A25IA é um projeto Android de IA pessoal adaptativa com foco em execução local, memória controlada, personalização profunda e evolução verificável.

## Estado atual — v0.2 Memory/Context

A baseline atual implementa:

- chat local;
- backend determinístico `adaptive-local-v2`;
- interface `InferenceBackend` para um LLM neural futuro;
- sessão temporária separada de memória de longo prazo;
- memória persistente apenas para fatos, preferências e objetivos;
- feedback persistido fora do contexto do modelo;
- migração conservadora da memória v0.1;
- ranking com stopwords, threshold de relevância, importância e recência;
- deduplicação/upsert por chave semântica;
- `IntentClassifier` com prioridade corrigida;
- `PersonalityCompiler`;
- personalidade base separada da adaptação aprendida;
- parâmetros completos editáveis;
- testes de regressão;
- CI com testes, Android Lint e build do APK.

## Problemas corrigidos a partir da v0.1

- perguntas sobre um tema não recebem mais conversas antigas irrelevantes apenas por recência;
- respostas completas do assistente deixam de ser memória permanente;
- feedback deixa de entrar no prompt;
- "oi quem é você?" é tratado como identidade, não só como saudação;
- 👍/👎 não troca mais o preset visual para Personalizada;
- memória deixa de crescer dois registros por turno;
- a UI de memória mostra somente conteúdo de longo prazo útil.

## Memória V2

~~~text
SessionMemory
  contexto recente, temporário e limitado

LongTermMemory
  FACT / PREFERENCE / GOAL

FeedbackStore
  avaliações separadas; não entram no prompt
~~~

## Personalidade

O usuário continua controlando:
- criatividade;
- verbosidade;
- empatia;
- assertividade;
- humor;
- formalidade;
- curiosidade;
- ceticismo;
- iniciativa.

Feedback altera somente offsets limitados de adaptação. O preset base permanece sob controle do usuário.

## Próxima etapa

Antes do primeiro LLM neural:
1. evoluir `InferenceBackend` para streaming/cancelamento/capabilities;
2. criar ModelManager;
3. criar DeviceCapabilityProbe;
4. criar BenchmarkHarness;
5. testar o mesmo modelo em runtimes locais concorrentes no Galaxy A25 5G.
