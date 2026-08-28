# Resultados de validação da Fase 7

## Automação

Executado em 28/08/2026:

- 29 testes backend, sem falhas, com migrations V1 a V6 em PostgreSQL 17.11 efêmero;
- lint, TypeScript estrito, 12 testes frontend em 6 arquivos e build Next.js de produção;
- Maven verify e JAR com SBOM CycloneDX;
- `pnpm audit --prod --audit-level high` sem vulnerabilidades conhecidas;
- OSV Scanner 2.4.0 fixado por digest sobre SBOM e lockfile, sem achados;
- `promtool` 3.14.0 fixado por digest, com cinco regras válidas;
- `git diff --check` sem erro.

## Staging sintético

- PostgreSQL isolado, vazio e descartável;
- migrations base até V6, sem seed `dev`;
- todas as flags externas desligadas;
- readiness `UP`, schema 6, request ID propagado e inválido substituído;
- métricas HTTP Prometheus disponíveis apenas por loopback;
- CSP, HSTS e `X-Content-Type-Options` presentes no frontend;
- logs estruturados com `requestId`, `traceId` e `spanId`.

O perfil staging recusou o banco local dev que possuía seed aplicada e checksum histórico divergente. Nenhum `repair` foi executado; o teste prosseguiu em banco limpo, confirmando o comportamento fail-closed.

## Carga

Teste de 10 segundos, concorrência 8:

- 31.446 requisições;
- zero erros;
- p95 de 4,67 ms;
- limite de aceite de 500 ms.

O resultado mede somente `/api/v1/system/status` local e não dimensiona produção.

## Recuperação e rollback

- backup sintético protegido por AES-256-GCM e hash SHA-256;
- restauração concluída em banco isolado;
- schema V6 verificado antes da limpeza automática do banco restaurado;
- commit estável `b7a9924` iniciou sobre V6, com readiness `UP`;
- processos, worktree e container de prova encerrados.

Dois arquivos criptografados de teste permaneceram em `work/` porque o host bloqueou a exclusão; a pasta é ignorada pelo Git, a chave não foi persistida e não foram usados dados reais.

## Segurança e dependências

A primeira leitura do SBOM identificou uma vulnerabilidade alta no PostgreSQL JDBC e seis médias. Foram atualizados PostgreSQL JDBC 42.7.12, Jackson 2.21.5, OpenTelemetry 1.62.0 e Log4j 2.25.5. Nova geração do SBOM retornou zero achados conhecidos.

## Riscos residuais

- staging externo, cofre, TLS, collector, Alertmanager e agenda de backup ainda não existem;
- a métrica de sucesso do backup precisa ser publicada pelo futuro agendador;
- a fonte de senhas comprometidas continua pendente;
- a validação financeira independente e a revisão jurídica permanecem obrigatórias antes do beta;
- o banco dev local anterior à consolidação da V5 precisa ser preservado e recriado de forma consciente, nunca reparado automaticamente.
