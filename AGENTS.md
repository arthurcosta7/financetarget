# Instruções do projeto Planejamento

## Objetivo

Construir um SaaS brasileiro de planejamento financeiro orientado a metas, com cálculos transparentes, segurança proporcional ao risco e arquitetura preparada para integrações sem acoplamento a fornecedores.

## Regra de fases

1. Leia `docs/STATUS.md`, `docs/ASSUMPTIONS.md`, o PRD e os ADRs antes de alterar o projeto.
2. Trabalhe somente na fase explicitamente autorizada.
3. Declare objetivo, escopo e critérios de aceite antes de implementar.
4. Atualize documentação, changelog e decisões na mesma mudança.
5. Rode as validações aplicáveis e apresente evidências.
6. Pare ao final da fase e aguarde aprovação explícita.

## Limites permanentes

- Preserve alterações preexistentes e não refatore itens fora do escopo.
- Não use dados pessoais ou financeiros reais em desenvolvimento e testes.
- Não trate projeções como garantias ou recomendações de investimento.
- Não use `float` ou `double` para dinheiro.
- Não coloque regras financeiras no frontend.
- Não acople domínio a DTOs, SDKs ou erros de provedores.
- Não hardcode segredos, URLs externas, preços, taxas, inflação, IDs de planos, moedas ou textos jurídicos.
- Não implemente microserviços sem evidência de necessidade.
- Não faça deploy, cobrança, envio real ou conexão bancária sem autorização.

## Direção técnica aprovada para planejamento

A stack-alvo, ainda sujeita à ADR na Fase 1, é Next.js/React/TypeScript no frontend, Spring Boot/Java no backend e PostgreSQL. A arquitetura inicial preferida é um monólito modular com contratos OpenAPI e portas para integrações.

## Qualidade

Toda regra crítica deve possuir critério verificável. Correções futuras devem incluir teste de regressão. Autorização, isolamento de dados, cálculos monetários, idempotência e migrations exigem testes explícitos.
