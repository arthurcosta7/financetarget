# Observabilidade e operação

## Objetivo

Permitir detectar falhas, investigar jornadas e medir capacidade sem registrar dados financeiros desnecessários.

## Padrão

Usar OpenTelemetry para traces e métricas quando a implementação começar. Bibliotecas de observabilidade ficam em adaptadores; o domínio emite eventos sem depender de fornecedor.

## Logs

Logs estruturados incluem:

- timestamp UTC;
- nível;
- serviço e versão;
- ambiente;
- `traceId` e `requestId`;
- código do evento;
- resultado e duração;
- IDs técnicos pseudonimizados quando necessários.

Nunca incluir senha, token, cookie, renda, patrimônio, valor da meta, premissas completas, texto livre ou payload integral de provedor.

## Métricas candidatas

- latência e taxa de erro por endpoint;
- conexões e duração de queries;
- falhas de autenticação agregadas;
- conflitos de edição;
- cálculos por tipo e duração;
- códigos de aviso do motor;
- convites criados, aceitos e expirados sem e-mail identificável;
- jobs futuros por estado, idade e retry;
- dependências externas por latência, erro e circuit breaker;
- SLI de disponibilidade e sucesso de jornada sintética.

Métricas de produto e telemetria operacional permanecem separadas. Nenhuma delas recebe valores financeiros.

## Traces

Propagar contexto entre frontend, API, banco e adaptadores. Spans usam nomes de operação estáveis e atributos allowlisted. Queries, corpos e headers sensíveis não são exportados.

## Health

- `liveness`: processo vivo, sem consulta cara.
- `readiness`: dependências essenciais mínimas, como PostgreSQL.
- integrações opcionais degradadas não derrubam readiness do núcleo manual.
- endpoint de health não expõe versão sensível, credencial ou topologia interna.

## Alertas iniciais

- erro 5xx acima do orçamento;
- p95 sustentado fora da meta;
- falha de login anômala;
- indisponibilidade de banco;
- falha de migration;
- backup ou restauração programada falhou;
- crescimento de jobs/retries futuros;
- cálculo produz taxa de erro ou aviso anômalo após release.

Todo alerta deve ter severidade, responsável, janela, condição de recuperação e runbook. Evitar alertas para eventos sem ação operacional.

## Auditoria versus logging

Auditoria registra fato de negócio sensível e retenção deliberada. Logging explica comportamento técnico e possui retenção curta. Um não substitui o outro.

## Ambientes

Dev prioriza diagnóstico local com dados sintéticos. Staging replica controles de produção sem dados reais. Produção aplica sampling, redaction, acesso mínimo e retenção aprovada.
