# Estado do projeto

## Resumo

- Fase atual: **Fase 5 — cenários e dashboard**.
- Estado: **concluída tecnicamente em 28/08/2026; aguardando aprovação**.
- Aplicação implementada: identidade, sessões, onboarding financeiro, privacidade, metas, Goal Engine, Scenario Engine, dashboard e comparação acessível.
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

A Fase 5 concluiu Scenario Engine, comparação, histórico, dashboard e múltiplas metas progressivas. O gate exige aprovação explícita; a Fase 6 não está autorizada. Colaboração operacional, integrações reais, pagamentos, notificações reais e deploy continuam fora do escopo.

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

## Entregáveis da Fase 4

| Entregável | Estado | Fonte |
|---|---|---|
| Goal Engine puro | Concluído | `planning/domain/` |
| Meta e snapshot imutável | Concluído | `goals/`, migration `V3` |
| Contribuição manual idempotente | Concluído | `GoalService`, `JdbcGoalRepository` |
| Autorização por espaço | Concluído | `GoalJourneyIntegrationTest` |
| Jornada web de metas | Concluído | `app/metas/`, componentes de meta |
| Contrato e tipos | Concluído | OpenAPI 0.3, `schema.d.ts` |
| Exportação LGPD ampliada | Concluído | `privacy/` |
| Evidências e decisão | Concluído | `docs/testing/PHASE-4-RESULTS.md`, ADR 0010 |

Fase 4 aprovada pelo usuário em 28/08/2026. O ADR 0010 foi aceito e a Fase 5 foi autorizada.

## Validações da Fase 4

Executadas em 28/08/2026:

- 17 testes backend pelo Maven em PostgreSQL 17.11 efêmero;
- 8 testes frontend, lint, TypeScript estrito e build de produção;
- OpenAPI 0.3 e tipos TypeScript regenerados;
- cálculo puro com dinheiro exato, versões e hash canônico;
- isolamento por espaço, CSRF e contribuição idempotente;
- exportação LGPD com meta e contribuição;
- inspeção visual em 1440 × 900 e 320 × 800 nos dois temas;
- correção e revalidação do reflow móvel das premissas;
- ausência de overflow horizontal e erros de console.

Evidências detalhadas estão em `docs/testing/PHASE-4-RESULTS.md`.

## Entregáveis da Fase 5

| Entregável | Estado | Fonte |
|---|---|---|
| Scenario Engine puro | Concluído | `planning/domain/Scenario*` |
| Cenários e snapshots | Concluído | `scenarios/`, migration `V4` |
| Isolamento do plano base | Concluído | repositórios e teste de regressão |
| Tipos progressivos de meta | Concluído | API, banco e formulário de meta |
| Dashboard | Concluído | `Dashboard.tsx`, `/app/inicio` |
| Comparação e histórico | Concluído | `ScenarioPlanner.tsx`, rota de cenários |
| Contrato e privacidade | Concluído | OpenAPI 0.4, exportação LGPD |
| Evidências e decisão | Concluído | `docs/testing/PHASE-5-RESULTS.md`, ADR 0011 proposto |

## Validações da Fase 5

Executadas em 28/08/2026:

- 20 testes backend pelo Maven em PostgreSQL 17.11 efêmero;
- 10 testes frontend, lint, TypeScript estrito e build de produção;
- migrations `V1` a `V4` e empacotamento do JAR;
- comparação determinística, limite de cenários e isolamento por espaço;
- cenário não altera o snapshot base, coberto por regressão;
- exportação LGPD inclui cenários do titular;
- dashboard e comparação inspecionados em 1440 × 900 e 320 × 800;
- temas claro e escuro, landmarks, tabela semântica e região rolável validados;
- ausência de overflow global e erros de console.

Evidências detalhadas estão em `docs/testing/PHASE-5-RESULTS.md`.

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
4. A validação matemática independente por especialista financeiro permanece obrigatória antes do beta.
