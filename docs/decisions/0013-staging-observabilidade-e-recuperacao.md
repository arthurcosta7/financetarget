# ADR 0013 — Staging fail-closed, observabilidade e recuperação

- Estado: Proposto
- Data: 28/08/2026

## Contexto

A Fase 7 precisa provar operação recuperável e reduzir riscos antes do beta, sem escolher plataforma ou provedor e sem introduzir dados ou efeitos reais.

## Decisão

Usar perfil staging isolado, somente com migrations base e flags externas desligadas. Uma guarda de startup rejeita cookies inseguros, CORS local/não HTTPS e integrações ativas em staging/produção.

Persistir janelas de tentativas de autenticação no PostgreSQL usando somente hash do identificador. Expor métricas Prometheus apenas por loopback, propagar request ID validado e correlação OpenTelemetry, e emitir logs ECS minimizados.

Gerar SBOM CycloneDX pelo Maven, auditar SBOM e lockfile na CI e validar regras Prometheus com imagens oficiais fixadas por digest. Backups locais/staging usam dump custom, AES-256-GCM e restauração obrigatória em banco isolado. Rollback de aplicação segue expand/contract, sem downgrade automático de migration.

## Alternativas

- rate limit somente em memória;
- Redis antes de existir outra necessidade operacional;
- métricas públicas protegidas apenas por obscuridade;
- logs de payload para facilitar diagnóstico;
- backup sem teste de restauração;
- escolher agora uma nuvem, APM, cofre ou provedor de alerta;
- rollback destrutivo do banco.

## Consequências

- múltiplas instâncias compartilham o limite sem novo componente;
- abuso intenso também consome escrita no banco e exige monitoramento;
- staging recusa defaults convenientes porém inseguros;
- a telemetria é portável, mas collector e Alertmanager continuam pendentes;
- SBOM aumenta transparência e tempo de CI;
- migrations devem permanecer compatíveis durante a janela de rollback.

## Riscos e mitigação

- crescimento da tabela de tentativas: expiração e limpeza oportunística;
- acesso indevido a métricas: loopback e teste negativo;
- falso verde do scanner Maven: SBOM resolvido pelo próprio Maven antes do OSV;
- perda de chave de backup: injeção por cofre futuro e teste regular de restauração;
- alerta de backup sem produtor: risco explicitado até implantação do agendador.

## Condições de revisão

Revisar ao adotar múltiplos bancos/regiões, collector/Alertmanager, cofre, plataforma de deploy, armazenamento de backup, Redis ou tráfego real.

## Aprovação

Aguardando aprovação explícita da Fase 7.
