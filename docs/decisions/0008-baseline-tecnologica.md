# ADR 0008 — Baseline tecnológica da Fase 2

- Estado: Aceito
- Data: 27/08/2026

## Contexto

A fundação precisa de versões reproduzíveis e compatíveis com as ferramentas LTS disponíveis. A versão mais nova nem sempre é a opção de menor risco para uma base de autenticação e dados financeiros.

## Decisão

Fixar inicialmente:

- Node.js 24 LTS;
- pnpm 11.19;
- Next.js 16.3.3;
- React 19.2.8;
- TypeScript 5.9.3;
- Java 25 LTS;
- Spring Boot 3.5.16;
- Maven Wrapper 3.9.16;
- PostgreSQL 17.11.

## Justificativa

Spring Boot 4.1.1 é a linha estável mais nova, mas a linha 3.5.16 possui ecossistema mais consolidado e compatibilidade oficial com Java 25. TypeScript 5.9 foi escolhido no lugar da linha 6 porque o gerador OpenAPI adotado ainda declara suporte à linha 5. PostgreSQL 17.11 foi escolhido no lugar da linha corrente 18.6 porque a versão de Flyway gerenciada pelo Spring Boot 3.5 ainda declara suporte testado até PostgreSQL 17. O projeto evita migrações de framework durante a criação dos fluxos críticos sem abrir mão de plataformas suportadas.

## Alternativas

- Spring Boot 4.1.1 imediatamente;
- Java 21 por compatibilidade máxima;
- dependências com ranges abertos ou tag `latest`;
- PostgreSQL 19 beta.
- PostgreSQL 18.6 com aviso de compatibilidade do Flyway.

## Consequências

- Lockfiles e wrappers tornam builds reproduzíveis.
- Atualizações de segurança continuam obrigatórias.
- A migração para Spring Boot 4 será avaliada antes do beta ou quando dependências essenciais estiverem validadas.

## Fontes de decisão

Documentação oficial consultada em 27/08/2026: Next.js 16, Node.js 24 LTS, Spring Boot 3.5.16 compatível até Java 25, PostgreSQL 18 como versão corrente e PostgreSQL 17.11 como a atualização da linha compatível selecionada. A compatibilidade prática do Flyway foi confirmada pelo teste de migration em container.
