# A25IA

A25IA é um projeto Android de IA pessoal adaptativa com foco inicial em execução local, memória persistente, personalização profunda e evolução verificável.

## Estado atual — v0.1 alpha

A baseline implementa:

- chat local;
- núcleo cognitivo determinístico (`adaptive-local-v1`);
- interface `InferenceBackend` para troca futura por um modelo neural local;
- memória persistente no armazenamento privado do aplicativo;
- recuperação de memória por relevância + recência + importância;
- extração conservadora de fatos explícitos;
- feedback positivo/negativo;
- autoajuste limitado e normalizado;
- perfis de personalidade;
- edição completa de parâmetros de geração e personalidade;
- DataStore para configurações;
- tela de diagnóstico/status;
- testes unitários;
- CI com testes, Android Lint e build do APK de debug.

## O que esta versão NÃO afirma

O backend incluído **não é um LLM** e não tenta fingir conhecimento generativo amplo. Ele é a baseline verificável do sistema adaptativo: memória, feedback, parâmetros, persistência, UI e contratos de inferência.

O próximo backend neural deverá implementar `InferenceBackend` e só substituir a baseline depois de comparação objetiva no dispositivo.

## Parâmetros editáveis

### Identidade
- nome da IA;
- idioma;
- instrução do sistema.

### Geração
- temperature;
- top-p;
- top-k;
- máximo de tokens;
- contexto;
- repetition penalty;
- presence penalty;
- frequency penalty;
- seed.

### Personalidade
- criatividade;
- verbosidade;
- empatia;
- assertividade;
- humor;
- formalidade;
- curiosidade;
- ceticismo;
- iniciativa.

Presets: Equilibrada, Técnica, Criativa, Direta, Amigável e Personalizada.

### Aprendizado
- memória;
- extração automática de fatos;
- aprendizado por feedback;
- autoajuste seguro;
- learning rate;
- ajuste máximo por feedback;
- quantidade de memórias recuperadas;
- limite total de memórias.

## Arquitetura

~~~
UI / Compose
     |
MainViewModel
     |
AiOrchestrator
  /       \
Memory   InferenceBackend
Store         |
          LocalAdaptiveBackend
          (substituível)
     |
SettingsRepository / DataStore
~~~

O backend de inferência não possui dependência da interface. Isso permite comparar LiteRT, ExecuTorch ou outro runtime sem reconstruir memória, configurações ou UI.

## Build

Requisitos de baseline:

- JDK 17
- Gradle 9.6
- Android Gradle Plugin 9.4
- Android SDK 36.1

No CI, o Gradle é instalado pela action oficial e o pipeline executa:

~~~
testDebugUnitTest
      ↓
lintDebug
      ↓
assembleDebug
      ↓
APK artifact
~~~

## Princípio de evolução

Nenhum autoajuste pode ultrapassar os limites normalizados definidos por `AiSettings`. Mudanças de arquitetura, runtime ou modelo devem ser medidas antes/depois e acompanhadas de testes de regressão.
