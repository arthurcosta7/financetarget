# Operação de produção

## Estado

Este documento é um contrato de implantação, não uma autorização de deploy. Nenhum provedor, domínio ou responsável foi definido. Os exemplos usam `.test`, não contêm segredos e não devem ser executados como configuração real.

## Topologia mínima

```text
Internet
  -> gateway/CDN com TLS
       -> /api/v1/* e health permitido -> API (rede privada)
       -> demais caminhos             -> Web (rede privada)
  API -> PostgreSQL privado
  API -> Resend por egress HTTPS controlado
  collector privado -> /actuator/prometheus em loopback/sidecar
  job isolado -> backup criptografado -> armazenamento imutável
```

O frontend é construído sem `NEXT_PUBLIC_API_BASE_URL`; chamadas usam `/api` na mesma origem. O gateway não pode expor PostgreSQL nem Prometheus. A plataforma deve aceitar somente tráfego do gateway para web/API e somente API/jobs para o banco.

## Secrets e identidades

- injetar segredos em runtime por cofre, nunca por imagem, manifesto Git ou argumento de build;
- separar identidade de runtime, migration, backup e restauração;
- limitar egress da API ao banco, DNS/telemetria aprovados e Resend;
- registrar proprietário, finalidade, criação, última rotação e próxima revisão de cada segredo;
- preferir credenciais curtas/dinâmicas quando a plataforma escolhida suportar;
- impedir leitura do segredo de backup pelo processo da aplicação.

## Artefatos e promoção

1. CI verde constrói uma vez as imagens do SHA aprovado.
2. Registry autorizado executa scan e registra digest, SBOM e atestação/assinatura.
3. Staging promove os mesmos digests e executa smoke com dados sintéticos.
4. O registro de release recebe digests, schema, backup, responsáveis e decisão.
5. Produção referencia digests, não tags mutáveis.
6. Readiness só recebe tráfego após migration e conexão mínima estarem saudáveis.

## Rollout gradual

Começar sem tráfego, executar smoke interno e então ampliar por etapas pequenas definidas pela plataforma. Em cada etapa, aguardar a janela aprovada e comparar disponibilidade, erros 5xx, p95, saturação, falhas de autenticação e erros do motor com a baseline. Interromper ao exceder qualquer orçamento aprovado; não aumentar recursos para mascarar uma regressão desconhecida.

## Rollback

- manter o digest anterior conhecido e testado contra o schema atual;
- pausar ampliação, registrar horário/impacto e confirmar compatibilidade;
- trocar o digest da aplicação sem executar downgrade de migration;
- repetir readiness e `production-smoke.ps1` com o SHA anterior;
- preservar logs, decisão e risco residual no registro do incidente/release.

## Backup e restauração

Os scripts locais continuam deliberadamente restritos a `dev` e `staging`. Produção requer job próprio da plataforma, armazenamento imutável, retenção aprovada, chave em cofre e restauração em banco isolado. Antes do tráfego, provar RPO/RTO acordados e publicar as métricas consumidas por `FinanceTargetBackupStale`.

## Observabilidade

O collector coleta métricas sem exposição pública, exporta traces/logs minimizados e encaminha as regras aprovadas ao Alertmanager. Cada alerta precisa de canal testado, responsável e substituto. Painéis não podem incluir e-mail, valores, títulos, payloads ou texto livre.

## Smoke autorizado

Após a infraestrutura existir, executar de uma estação autorizada:

```powershell
./scripts/production-smoke.ps1 `
  -ApiBaseUrl https://app.example.test `
  -WebBaseUrl https://app.example.test `
  -ExpectedReleaseId <sha-git-completo> `
  -ExpectedSchemaVersion 7 `
  -VerifyGatewayRoute
```

O smoke é somente leitura. Substitua o domínio de exemplo apenas depois da autorização de infraestrutura.
