# Estado do projeto

## Resumo

- Fase atual: **Fase 4 — fatia vertical de metas**.
- Estado: **autorizada em 27/08/2026; em implementação**.
- Aplicação implementada: identidade, sessões, onboarding financeiro mínimo e privacidade inicial.
- Integrações reais: não.
- Dados reais: não.
- Deploy: não.
- Repositório Git: inicializado localmente em 27/08/2026.

## Entregáveis da Fase 0

| Entregável | Estado | Fonte |
|---|---|---|
| Auditoria inicial | Concluído | Este documento |
| Descoberta e tensões | Concluído | `docs/product/DISCOVERY.md` |
| Personas e JTBD | Concluído | `docs/product/PERSONAS.md` |
| Plano de validação | Concluído | `docs/product/VALIDATION-PLAN.md` |
| PRD inicial | Concluído | `docs/product/PRD.md` |
| Roadmap | Concluído | `docs/product/ROADMAP.md` |
| Premissas | Concluído | `docs/ASSUMPTIONS.md` |
| Decision log e ADR inicial | Concluído | `docs/decisions/` |
| Changelog | Concluído | `CHANGELOG.md` |

Fase 0 aprovada pelo usuário em 27/08/2026.

## Entregáveis da Fase 1

| Entregável | Estado | Fonte |
|---|---|---|
| Arquitetura e módulos | Concluído | `docs/architecture/OVERVIEW.md`, `MODULES.md` |
| Modelo de domínio e dados | Concluído | `docs/architecture/DOMAIN-MODEL.md` |
| Goal Engine e Scenario Engine | Concluído | `docs/architecture/CALCULATION-ENGINES.md` |
| API e hubs | Concluído | `docs/api/STRATEGY.md`, `docs/architecture/INTEGRATION-HUBS.md` |
| Configuração e ambientes | Concluído | `docs/architecture/CONFIGURATION.md` |
| Observabilidade | Concluído | `docs/architecture/OBSERVABILITY.md` |
| Autenticação | Concluído | `docs/security/AUTHENTICATION.md` |
| Threat model | Concluído | `docs/security/THREAT-MODEL.md` |
| Plano LGPD | Concluído | `docs/security/LGPD.md` |
| Fluxos e mapa de telas | Concluído | `docs/ux/EXPERIENCE-STRATEGY.md`, `SCREEN-MAP.md` |
| Design system | Concluído | `docs/ux/DESIGN-SYSTEM.md` |
| Wireframes | Concluído | `docs/ux/WIREFRAMES.md` |
| Preflight visual | Concluído, sem signoff final | `docs/ux/VISUAL-PREFLIGHT.md` |
| Matriz de testes | Concluído | `docs/testing/PHASE-1-MATRIX.md` |
| ADRs técnicos | Concluído | `docs/decisions/0002` a `0007` |

## Auditoria inicial

A pasta estava vazia, sem repositório Git, instruções, código ou documentação. Não havia alterações a preservar. O projeto Redator foi consultado somente como referência de disciplina documental, invariantes, segurança e testes; nenhum código ou decisão específica de domínio foi copiado.

## Aprovação da Fase 1

A Fase 1 foi aprovada pelo usuário em 27/08/2026. A Fase 2 está autorizada com as decisões abaixo aceitas:

1. monólito modular e limites dos módulos;
2. autenticação local com possibilidade de OIDC futuro;
3. convenções do Goal Engine e snapshots imutáveis;
4. `PlanningSpace` e papéis de colaboração;
5. estratégia REST/OpenAPI e hubs;
6. direção visual “precisão calma”, fontes e linha de trajetória;
7. regras ainda abertas de exclusão e saída em espaços compartilhados permanecem para validação antes do fluxo correspondente.

## Validações da Fase 0

Executadas em 27/08/2026:

- presença de todos os artefatos obrigatórios;
- resolução dos links Markdown locais;
- parsing do pacote estratégico JSON;
- verificação de whitespace e conflitos pelo Git;
- revisão cruzada de persona, proposta de valor, MVP, não objetivos, hipóteses, riscos e roadmap.

Não existiam validações de build ou testes de aplicação na Fase 0 porque nenhum código de produto foi criado.

## Entregáveis da Fase 2

| Entregável | Estado | Fonte |
|---|---|---|
| Workspace e versões reproduzíveis | Concluído | `package.json`, `pnpm-lock.yaml`, `.nvmrc`, `.java-version` |
| Frontend Next.js | Concluído | `apps/web/` |
| API Spring Boot modular | Concluído | `apps/api/` |
| PostgreSQL e migrations | Concluído | `compose.yaml`, `apps/api/src/main/resources/db/` |
| OpenAPI e tipos gerados | Concluído | `openapi.yaml`, `schema.d.ts` |
| Segurança e health base | Concluído | `SecurityConfiguration`, `application.yml`, `next.config.ts` |
| Testes frontend e backend | Concluído | testes Vitest e Spring/Testcontainers |
| CI e atualização de dependências | Concluído | `.github/workflows/ci.yml`, `.github/dependabot.yml` |
| Setup e resultados | Concluído | `docs/DEVELOPMENT.md`, `docs/testing/PHASE-2-RESULTS.md` |

## Gate atual

A Fase 3 foi aprovada em 27/08/2026 após demonstrar isolamento de dados e fluxos negativos. A Fase 4 está autorizada somente para a primeira fatia vertical de metas e o Goal Engine. Scenario Engine, colaboração operacional, integrações reais, pagamentos e deploy continuam fora do escopo.

## Entregáveis da Fase 3

| Entregável | Estado | Fonte |
|---|---|---|
| Identidade e sessões | Concluído | `identity/`, `SecurityConfiguration` |
| Espaço pessoal | Concluído | `planningspace/`, migration `V2` |
| Perfil financeiro mínimo | Concluído | `profile/` |
| Consentimento e auditoria | Concluído | `consent_record`, `audit/` |
| Exportação e desenho de exclusão | Concluído | `privacy/` |
| Jornadas web | Concluído | `cadastro/`, `entrar/`, `app/onboarding/`, `app/conta/` |
| Contrato e configuração | Concluído | OpenAPI 0.2, `.env.example`, properties |
| Segurança do frontend | Concluído | `proxy.ts`, CSP com nonce |
| Testes e inspeção visual | Concluído | `docs/testing/PHASE-3-RESULTS.md` |
| Guia de execução local | Concluído | `README.md`, `docs/DEVELOPMENT.md` |

Fase 3 aprovada pelo usuário em 27/08/2026. O ADR 0009 foi aceito com a condição de revisão já registrada antes do beta.

## Validações da Fase 3

Executadas em 27/08/2026:

- 9 testes de integração backend em PostgreSQL 17.11 efêmero;
- 6 testes frontend, lint, TypeScript estrito e build de produção;
- OpenAPI e tipos TypeScript regenerados;
- uso único de verificação e recuperação;
- rotação e detecção de reuso de refresh;
- CSRF real, cookies HttpOnly/SameSite e autorização fechada;
- isolamento do perfil e da exportação entre duas contas sintéticas;
- idempotência da solicitação de exclusão;
- inspeção visual em 1440 × 900 e 320 × 800 nos dois temas;
- ausência de overflow horizontal e erros de console;
- revisão de hardcodes, dinheiro exato e whitespace.

Evidências detalhadas estão em `docs/testing/PHASE-3-RESULTS.md`.

## Validações da Fase 1

Executadas em 27/08/2026:

- presença dos 16 entregáveis centrais da fase;
- parsing dos cinco contratos JSON e verificação de campos obrigatórios do `DesignContext` e dos `DesignSignalPacket`;
- links Markdown locais resolvidos;
- blocos Markdown balanceados;
- índice consistente com sete ADRs;
- 33 requisitos funcionais sem IDs duplicados;
- sintaxe JavaScript e contrato de fragmento do wireframe interativo;
- verificação de whitespace pelo Git;
- revisão cruzada de colaboração, motores, API, hubs, autenticação, LGPD, acessibilidade e direção visual.

Não houve build ou teste de aplicação porque a Fase 1 não criou código de produção. O preflight visual não equivale a signoff: contraste, reflow, foco e acabamento deverão ser inspecionados na implementação da Fase 2.

## Validações da Fase 2

Executadas em 27/08/2026:

- geração determinística dos tipos TypeScript a partir do OpenAPI;
- lint, TypeScript estrito, testes de componentes e build de produção do frontend;
- Maven Enforcer, compilação, testes de integração e empacotamento do backend;
- migration Flyway executada em PostgreSQL 17.11 efêmero e local;
- autorização fechada por padrão e allowlist de CORS verificadas;
- smoke test do caminho web–API–PostgreSQL e health check;
- inspeção visual em 1440 × 900 e 320 × 800, nos dois temas;
- ausência de overflow horizontal e erros no console;
- auditoria de dependências de produção e verificação de whitespace.

Evidências detalhadas estão em `docs/testing/PHASE-2-RESULTS.md`.

## Questões abertas

1. Rate limit distribuído e fonte de senhas comprometidas precisam ser definidos antes do beta.
2. ESLint 9 é uma exceção temporária de compatibilidade; migrar quando os plugins usados pelo Next.js declararem suporte ao ESLint 10.
3. Exclusão e saída de um dos membros de espaço compartilhado continuam aguardando validação antes da remoção física.
