# Arquitetura A25IA v0.1

## Objetivo

Manter separadas cinco responsabilidades:

1. UI;
2. estado/configuração;
3. memória;
4. orquestração;
5. inferência.

## Contrato de inferência

`InferenceBackend` é o limite arquitetural entre o produto Android e o motor cognitivo.

Um backend futuro deve receber `InferenceRequest` e devolver `InferenceResult`. Isso impede que um runtime de IA contamine UI, persistência ou memória com APIs específicas.

## Memória

A memória v0.1 é local e privada ao app. O ranking usa:

- sobreposição lexical;
- importância;
- recência.

É propositalmente simples e mensurável. Embeddings não foram adicionados sem benchmark.

## Aprendizado

O feedback não altera código nem executa patches.

Ele ajusta somente parâmetros explicitamente permitidos, usando:

`passo = min(learningRate, maxAdjustmentPerFeedback)`

e todos os valores passam por `normalized()`.

## Segurança

- nenhuma chave ou token;
- nenhuma permissão de rede;
- cleartext desativado;
- dados no armazenamento privado;
- limites explícitos para todos os parâmetros adaptativos.

## Próximo limite arquitetural

Adicionar um backend neural local sem modificar o contrato `InferenceBackend`, seguido de A/B contra `adaptive-local-v1`.
