# Validação

## Regressões cobertas no Memory/Context V2

Os testes unitários verificam:

- clamp das configurações e offsets aprendidos;
- identidade vencendo saudação em "oi quem é você?";
- perguntas de memória vencendo pergunta genérica;
- uma pergunta sobre o sol não recuperando memórias sobre água/nuvens;
- ranking de memória relevante;
- extração estruturada de nome, preferência e objetivo;
- feedback preservando preset e personalidade base;
- limite do contexto de sessão.

## CI obrigatório

O workflow .github/workflows/ci.yml exige:

1. testes unitários;
2. Android Lint com warnings tratados como erro;
3. build debug;
4. publicação do APK como artifact.

## Requer validação no aparelho

Ainda precisa ser medido no Galaxy A25 5G:

- migração de dados de uma instalação v0.1 real;
- UX da tela Memória V2;
- uso de RAM em sessão longa;
- estabilidade com centenas de memórias úteis;
- temperatura;
- bateria;
- TTFT e tokens/s quando o backend neural existir.
