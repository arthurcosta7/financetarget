# Resposta a incidentes

## Princípios

Priorizar segurança das pessoas e integridade dos dados, conter sem destruir evidências, comunicar fatos confirmados e manter um registro temporal das decisões. Este runbook não substitui avaliação jurídica ou especializada.

## Severidade

| Nível | Exemplo | Resposta inicial proposta |
|---|---|---|
| Crítico | indisponibilidade ampla, possível exposição, corrupção | reconhecer em 15 min |
| Alto | jornada essencial degradada, abuso ativo contido | reconhecer em 30 min |
| Médio | falha parcial com alternativa segura | próximo período operacional |

Os tempos são metas provisórias e dependem de responsáveis definidos antes do beta.

## Fluxo comum

1. Abrir registro com horário UTC, alerta, versão e ambiente.
2. Nomear responsável pelo incidente e pessoa de comunicação.
3. Confirmar impacto com métricas, traces e logs minimizados.
4. Conter: bloquear tráfego abusivo, desabilitar flag ou reverter artefato conforme o caso.
5. Preservar evidências sem copiar segredos ou dados financeiros para tickets.
6. Recuperar e executar smoke test.
7. Avaliar obrigação de comunicação com segurança e jurídico.
8. Registrar causa raiz, ações, proprietário e prazo.

## API indisponível

- confirmar liveness, readiness e conectividade com PostgreSQL;
- verificar mudança recente, saturação e falha de migration;
- não marcar readiness como saudável removendo uma dependência essencial;
- reverter somente para artefato comprovadamente compatível com o schema atual;
- validar `/actuator/health/readiness` e `/api/v1/system/status` após recuperação.

## Erros 5xx

- segmentar por rota, versão e janela, sem usar payloads;
- correlacionar por `requestId`, `traceId` e `spanId`;
- verificar pool, timeouts e release recente;
- reduzir ou interromper rollout antes de aumentar capacidade sem evidência.

## Latência

- confirmar p95 sustentado, volume e taxa de erros;
- inspecionar pool e dependências por operação;
- comparar com baseline sintético e release anterior;
- não mascarar a causa elevando indiscriminadamente timeouts.

## Banco

- confirmar saturação, conexões, disco e queries lentas por metadados;
- interromper migrations concorrentes;
- não executar `repair`, downgrade destrutivo ou restauração sobre o banco original;
- restaurar primeiro em banco isolado e seguir `BACKUP-RESTORE.md`.

## Suspeita de incidente LGPD

Conter acesso, preservar a trilha mínima, identificar categorias e titulares potencialmente afetados e envolver avaliação jurídica. Não declarar ausência de impacto antes da investigação. Registrar decisão sobre notificações aplicáveis.

## Encerramento

O incidente só termina após serviço estável, smoke aprovado, risco residual aceito e ações de regressão registradas. Correções de software devem incluir teste.
