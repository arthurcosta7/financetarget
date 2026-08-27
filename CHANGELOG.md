# Changelog

Todas as mudanças relevantes deste projeto serão registradas aqui.

## [Unreleased]

### Added

- Plano executável e ADR proposto para a Fase 3 de identidade e onboarding.
- Cadastro, verificação, login, cookies HttpOnly, CSRF, renovação rotativa, logout e recuperação de acesso.
- Hash de senha Argon2id, rate limit inicial, auditoria modular e CSP com nonce por requisição.
- Espaço pessoal provisionado pelo módulo de planejamento após verificação.
- Perfil financeiro mínimo com dinheiro exato, capacidade estimada no backend e consentimentos versionados.
- Exportação de dados próprios e solicitação idempotente de exclusão com reautenticação.
- Jornadas responsivas de cadastro, acesso, recuperação, onboarding e conta nos dois temas.
- Migration `V2`, contrato OpenAPI 0.2, tipos gerados e testes de isolamento e fluxos negativos.
- Evidências técnicas e visuais da Fase 3.
- Fundação documental da Fase 0.
- Descoberta estratégica, tensões, JTBD e proposta de valor inicial.
- Quatro personas comportamentais e escolha provisória da persona primária.
- PRD inicial com escopo, requisitos, não objetivos, métricas e riscos.
- Plano de validação por entrevistas, landing page e teste de preço.
- Roadmap de Fase 0 a produção e ondas futuras de integração.
- Registro explícito de premissas e decisões.
- Regras operacionais em `AGENTS.md`.
- Arquitetura de monólito modular, limites de contexto e metas operacionais iniciais.
- Estratégia de configuração, ambientes, feature flags e observabilidade.
- Modelo de domínio com espaços pessoais e compartilhados.
- Especificação matemática do Goal Engine e do Scenario Engine.
- Estratégia REST/OpenAPI e hubs independentes de provedores.
- Arquitetura de autenticação, threat model e plano inicial de LGPD.
- Estratégia de experiência, mapa de telas e wireframes de baixa fidelidade.
- Design system monocromático, invertível e com preflight anti-slop.
- Matriz de testes derivada dos riscos da arquitetura.
- ADRs 0002 a 0007.
- Configuração inicial de Git, incluindo normalização de finais de linha e exclusão de segredos, builds e arquivos locais.
- Workspace pnpm, Next.js 16, React 19 e TypeScript 5.9.
- API Spring Boot 3.5 em Java 25, organizada por domínio, aplicação e adaptadores.
- PostgreSQL 17.11, Compose, Flyway e seed exclusivamente sintético de desenvolvimento.
- Contrato OpenAPI e geração versionada de tipos para o frontend.
- Primeira integração técnica web–API–banco e health checks.
- Segurança fechada por padrão, allowlist de CORS e headers defensivos no frontend.
- Interface monocromática, responsiva e invertível com linha de trajetória.
- Testes Vitest, Spring Boot e Testcontainers, pipeline de CI e Dependabot.
- Scripts e documentação de desenvolvimento e validação da Fase 2.

### Changed

- Fase 0 marcada como aprovada.
- PRD atualizado para incluir metas compartilhadas por casal no MVP.
- Roadmap atualizado com o estado da Fase 1.
- ADRs 0002 a 0007 aceitos após aprovação da Fase 1.
- Baseline ajustada para TypeScript 5.9 e PostgreSQL 17.11 por compatibilidade declarada das ferramentas.
- Fase 2 marcada como concluída e aguardando aprovação.
- Fase 2 aprovada e Fase 3 autorizada em 27/08/2026.
- Fase 3 concluída tecnicamente e mantida no gate de aprovação.
- Persistência executável simplificada para Spring JDBC, preservando portas de aplicação e módulos.
