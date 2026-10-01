# Validação

## Cobertura implementada

Os testes unitários verificam:

- normalização/clamp de configurações;
- ranking de memória;
- limite de adaptação por feedback;
- extração explícita de fatos.

## CI

O workflow `.github/workflows/ci.yml` exige:

1. testes unitários;
2. Android Lint com warnings tratados como erro;
3. build debug;
4. publicação do APK como artifact da execução.

## Ainda requer dispositivo real

Não declarar como validado sem medição no Galaxy A25 5G:

- temperatura;
- bateria;
- RAM de pico;
- latência de UI em sessões longas;
- estabilidade após centenas/milhares de memórias;
- runtime neural;
- tokens/s;
- TTFT.

Esses itens formam a baseline de dispositivo da próxima etapa.
