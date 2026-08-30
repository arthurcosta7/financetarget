# Roadmap por fases

O roadmap descreve sequência e gates, não datas prometidas. Prazos serão estimados após a Fase 1 e revisão da capacidade disponível.

## Fase 0 — Descoberta e fundação do produto

**Estado:** aprovado em 27/08/2026.

Personas, JTBD, hipóteses, descoberta, PRD, plano de validação, riscos, escopo, não objetivos e governança documental.

**Gate:** coerência dos artefatos e aprovação explícita.

## Fase 1 — Arquitetura, segurança e direção de UX

Definir módulos, domínio, contratos, Goal Engine, Scenario Engine, autenticação, threat model, LGPD, hubs, modelo de dados, fluxos e wireframes.

**Estado:** aprovada em 27/08/2026.

**Gate:** limites claros, riscos críticos mitigados no desenho e jornada revisável.

## Fase 2 — Fundação técnica

Criar monorepo, Next.js/React/TypeScript, Spring Boot/Java, PostgreSQL, containers, migrations, OpenAPI, CI, health checks, seeds e mocks.

**Estado:** aprovada em 27/08/2026.

**Gate:** setup reproduzível e primeira integração técnica frontend–API–banco testada.

## Fase 3 — Identidade e onboarding

Cadastro, verificação, login, sessões, recuperação, perfil financeiro mínimo, consentimentos, auditoria, exportação e desenho de exclusão.

**Estado:** aprovada em 27/08/2026.

**Gate:** isolamento de dados e fluxos negativos demonstrados.

## Fase 4 — Fatia vertical de metas

Criar e acompanhar uma meta inicial, implementar Goal Engine, snapshots, explicação das premissas e testes independentes dos cálculos.

**Estado:** aprovada em 28/08/2026.

**Gate:** jornada ponta a ponta, cálculo reproduzível e nenhuma regra financeira na UI.

## Fase 5 — Cenários e dashboard

Scenario Engine, comparação, gráficos acessíveis, histórico, múltiplas metas progressivas e revisão visual responsiva.

**Estado:** aprovada em 28/08/2026.

**Gate:** compreensão de projeção testada e cenários reproduzíveis.

## Fase 6 — Assinaturas, notificações e hubs simulados

Entitlements, PaymentsHub mock, NotificationHub mock, webhooks idempotentes, preferências, feature flags e contratos dos demais hubs.

**Estado:** aprovada em 28/08/2026.

**Gate:** nenhum provedor real no domínio e falhas simuláveis.

## Fase 7 — Hardening e staging

Staging, testes de carga, observabilidade, alertas, backups, restauração, rollback, auditoria de segurança, runbooks e revisão LGPD.

**Estado:** aprovada em 28/08/2026.

**Gate:** vulnerabilidades críticas resolvidas e operação recuperável demonstrada.

## Fase 8 — Beta fechado

Grupo controlado, analytics sem dados financeiros sensíveis, suporte, feedback, monitoramento, termos revisados e critérios de aprendizagem.

**Estado:** preparação técnica concluída em 30/08/2026 e aguardando aprovação. A entrada de usuários reais depende dos gates humano, jurídico, operacional e matemático documentados.

**Gate:** decisão documentada de prosseguir, ajustar ou interromper.

## Fase 9 — Produção

Ambiente isolado, secrets, domínio, TLS, backups, observabilidade, deploy gradual, smoke tests, rollback e lançamento aprovado.

**Gate:** checklist manual de produção e responsabilidades operacionais definidas.

## Ondas futuras de integração

Cada integração requer ADR, análise jurídica/segurança/custo, sandbox, adaptador isolado, testes de contrato, feature flag e liberação gradual.

1. Pagamentos reais.
2. Notificações reais.
3. Open Finance por parceiro autorizado.
4. Milhas e fidelidade.
5. Viagens.
6. Financiamento imobiliário.
7. Financiamento automotivo.

A ordem poderá mudar conforme evidência de valor e dependências regulatórias.
