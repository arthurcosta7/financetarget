# ADR 0005 — API contratual e abstração por hubs

- Estado: Aceito
- Data: 27/08/2026

## Contexto

Open Finance, pagamentos, notificações, fidelidade, viagens e financiamento podem ter múltiplos fornecedores e contratos instáveis.

## Decisão

Usar REST `/api/v1` documentada por OpenAPI e portas internas canônicas para cada família de integração. Adaptadores isolam SDKs, DTOs, autenticação e erros externos. Valores decimais trafegam como strings.

## Alternativas

- chamar SDKs diretamente em services de domínio;
- criar um gateway genérico universal;
- GraphQL como contrato inicial.

## Consequências

- Troca e teste de fornecedores ficam localizados.
- Exige mapeamento explícito e testes de contrato.
- Não elimina diferenças reais entre provedores; o modelo canônico deve permanecer mínimo.

## Revisão

Cada integração real exige ADR próprio e pode motivar extensão localizada do hub.
