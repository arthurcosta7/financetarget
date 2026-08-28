# Fase 7 — Hardening e staging

## Objetivo

Tornar a base tecnicamente verificável para um futuro beta, com configuração fail-closed, telemetria minimizada, auditoria de dependências e recuperação demonstrável.

## Escopo executado

- perfil staging sem seed dev e com logs ECS;
- guarda de ambiente para cookies, HTTPS, CORS e flags;
- rate limit de autenticação distribuído pelo PostgreSQL, sem e-mail persistido;
- request ID validado, traces OpenTelemetry e métricas Prometheus;
- headers web adicionais;
- regras de alerta e runbooks;
- SBOM CycloneDX, pnpm audit e OSV Scanner na CI;
- backup criptografado, restauração isolada e prova de rollback compatível;
- smoke e carga sintética;
- revisão do threat model e LGPD.

Ficaram fora: deploy externo, domínio/TLS real, cofre, Alertmanager, armazenamento de backup, dados reais, integrações, cobrança, envio e beta.

## Critérios de aceite

| Critério | Evidência |
|---|---|
| Staging falha fechado | testes da guarda e perfil staging com flags desligadas |
| Dados dev não entram em staging | migration base separada e teste sem catálogo sintético |
| Abuso de login é coordenado entre instâncias | janela persistida por hash no PostgreSQL e teste de 429 |
| Telemetria não exige dados financeiros | métricas técnicas, MDC e política de allowlist |
| Métricas não ficam públicas | acesso Prometheus restrito a loopback e teste negativo remoto |
| Dependências conhecidas estão auditadas | SBOM de 82 componentes e OSV/pnpm sem achados após correções |
| Operação é recuperável | backup AES-GCM restaurado em schema V6 isolado |
| Rollback é compatível | artefato `b7a9924` saudável sobre schema V6 |
| Capacidade básica é conhecida | carga sintética com limiar explícito e zero erros |
| Alertas são acionáveis | cinco regras validadas por `promtool` e ligadas a runbooks |

## Gate

Implementação e validação técnica concluídas em 28/08/2026. A Fase 7 aguarda aprovação explícita antes da Fase 8.
