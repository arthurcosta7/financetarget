# Fase 2 — fundação técnica

**Estado:** aprovada pelo usuário em 27/08/2026.

## Objetivo

Criar uma base reproduzível para web, API e PostgreSQL e provar um primeiro caminho técnico completo sem antecipar autenticação, motores financeiros ou integrações externas.

## Escopo executado

- workspace com versões fixadas e lockfile;
- frontend Next.js/React/TypeScript com tema claro/escuro e direção visual aprovada;
- backend Spring Boot/Java organizado em domínio, aplicação e adaptadores;
- PostgreSQL local, migration versionada e seed exclusivamente sintético;
- contrato OpenAPI e tipos TypeScript gerados;
- endpoint técnico web–API–banco;
- autorização fechada por padrão e CORS por allowlist;
- health checks sem detalhes internos;
- testes de componente e integração com banco efêmero;
- pipeline de CI, atualização automatizada de dependências e scripts locais;
- documentação de desenvolvimento e evidências de validação.

## Critérios de aceite

| Critério | Evidência esperada |
|---|---|
| Setup reproduzível | versões, wrapper, lockfile, `.env.example` e scripts versionados |
| Frontend compilável | lint, typecheck, testes e build aprovados |
| API compilável | Maven Wrapper executa testes e gera pacote |
| Banco versionado | Flyway aplica `V1` em PostgreSQL efêmero e local |
| Contrato sincronizado | tipos web regeneráveis a partir do OpenAPI sem diff |
| Integração completa | tela exibe web, API, PostgreSQL e schema conectados |
| Segurança básica | endpoints fechados por padrão, CORS restrito e headers defensivos |
| UX base | temas invertíveis, 320 px sem overflow, foco visível e landmarks semânticos |

## Fora de escopo

- cadastro, login, sessões e consentimentos;
- tabelas e jornadas de metas;
- Goal Engine e Scenario Engine executáveis;
- integrações, pagamentos ou notificações reais;
- staging, produção, publicação ou deploy;
- dados pessoais ou financeiros reais.

## Estrutura

```text
apps/
  api/      Spring Boot e contrato OpenAPI
  web/      Next.js e tipos gerados do contrato
docs/       produto, arquitetura, segurança, UX e decisões
scripts/    inicialização e validação local
```

O primeiro módulo executável, `system`, demonstra a direção do monólito modular. Sua porta de leitura está na camada de aplicação; JDBC e HTTP são adaptadores e não contaminam o domínio.

## Questões adiadas conscientemente

- CSP com nonce será definida junto da sessão autenticada na Fase 3.
- O fluxo de saída e exclusão de espaços compartilhados continua dependente da validação prevista no PRD.
- ESLint 9 permanece temporariamente por compatibilidade declarada dos plugins do Next.js; a migração para ESLint 10 deve ser reavaliada quando o ecossistema suportar a linha.
