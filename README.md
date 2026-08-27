# Planejamento

SaaS brasileiro de planejamento financeiro por metas. O produto pretende transformar objetivos de vida em planos compreensíveis, mostrando quanto guardar, por quanto tempo e como mudanças de prazo, aporte e premissas alteram a projeção.

## Estado atual

O projeto concluiu a **Fase 2 — fundação técnica** e aguarda aprovação para a Fase 3. Há uma interface inicial, API, PostgreSQL, migration, contrato OpenAPI, testes e CI; autenticação e funcionalidades financeiras ainda não foram implementadas.

Os artefatos centrais desta fase são:

- [PRD](docs/product/PRD.md);
- [descoberta estratégica](docs/product/DISCOVERY.md);
- [personas](docs/product/PERSONAS.md);
- [plano de validação](docs/product/VALIDATION-PLAN.md);
- [roadmap](docs/product/ROADMAP.md);
- [arquitetura](docs/architecture/OVERVIEW.md);
- [motores de cálculo](docs/architecture/CALCULATION-ENGINES.md);
- [configuração e ambientes](docs/architecture/CONFIGURATION.md);
- [observabilidade](docs/architecture/OBSERVABILITY.md);
- [segurança e LGPD](docs/security/THREAT-MODEL.md);
- [direção visual](docs/ux/DESIGN-SYSTEM.md);
- [wireframes](docs/ux/WIREFRAMES.md);
- [premissas](docs/ASSUMPTIONS.md);
- [estado do projeto](docs/STATUS.md);
- [registro de decisões](docs/decisions/README.md).
- [desenvolvimento local](docs/DEVELOPMENT.md);
- [escopo da Fase 2](docs/implementation/PHASE-2.md);
- [resultados de validação da Fase 2](docs/testing/PHASE-2-RESULTS.md).

## Princípio de trabalho

O desenvolvimento é conduzido por fases. Uma fase só avança após revisão dos entregáveis, validações aplicáveis, atualização da documentação e aprovação explícita.

## Próximo passo

Revisar a Fase 2. Após aprovação explícita, a Fase 3 poderá implementar identidade, sessões, consentimentos e onboarding com os testes de isolamento previstos.
