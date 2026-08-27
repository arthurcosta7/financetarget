# ADR 0002 — Monólito modular como arquitetura inicial

- Estado: Aceito
- Data: 27/08/2026

## Contexto

O produto possui domínios distintos e integrações futuras, mas ainda não há carga, equipe ou requisitos que justifiquem distribuição.

## Decisão

Usar um monólito modular Spring Boot com fronteiras por contexto, arquitetura hexagonal e PostgreSQL como fonte de verdade. O frontend Next.js é uma aplicação separada, mas não contém regras financeiras nem autorização final.

## Alternativas

- microserviços desde o início;
- monólito em camadas técnicas globais;
- funções serverless por caso de uso.

## Consequências

- Menor custo operacional e transações locais simples.
- Requer disciplina de módulos e testes arquiteturais.
- Extração futura permanece possível, mas não gratuita.

## Revisão

Reavaliar somente com evidência de escala, disponibilidade, segurança, equipe ou deploy independente.
