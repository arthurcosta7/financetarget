# Release e rollback

## Estratégia

Promover o mesmo artefato imutável entre ambientes. Banco usa mudanças expand/contract: uma versão anterior precisa continuar funcionando durante a janela de rollback. Migrations aplicadas não são desfeitas automaticamente.

Na Fase 9, web e API são imagens OCI sem root, identificadas pelo SHA Git completo. A promoção futura usa os digests do registry e o registro em `RELEASE-RECORD-TEMPLATE.md`; tags servem apenas como referência humana.

## Pré-release

1. CI verde, SBOM gerado e auditoria sem achado bloqueante.
2. Artefato identificado por commit e hash.
3. Migration revisada como aditiva ou com plano explícito de compatibilidade.
4. Backup recente com restauração verificada.
5. Smoke e carga curta aprovados em staging sintético.
6. Alertas e responsável da janela confirmados.

## Liberação futura

Quando existir plataforma autorizada, usar rollout gradual. Readiness deve impedir tráfego antes da migration concluída. Comparar erros, latência e saturação com a versão anterior antes de ampliar.

O smoke de produção compara o `releaseId` exposto pela API com o SHA aprovado. O gateway público também deve provar o encaminhamento same-origin de `/api/v1` antes de receber tráfego.

## Decisão de rollback

Reverter o artefato quando houver regressão confirmada e a versão anterior estiver testada contra o schema atual. Se houver escrita incompatível, interromper o rollout e seguir plano específico; não forçar downgrade de schema.

Após rollback:

- confirmar readiness e status;
- executar `staging-smoke.ps1` ou equivalente do ambiente;
- verificar jornadas afetadas e métricas;
- registrar versão, horário, motivo e risco residual.

## Evidência da Fase 7

O commit estável `b7a9924` da Fase 6 foi compilado em worktree descartável e iniciou sobre schema V6. Readiness ficou `UP` e `/api/v1/system/status` reportou versão 6. A migration V6 é aditiva, portanto o rollback do binário foi compatível.

Nenhum deploy externo ou rollback de plataforma ocorreu nesta fase.
