# ADR 0016 — Artefatos imutáveis e gateway same-origin

- Estado: Proposto
- Data: 31/08/2026

## Contexto

A Fase 9 precisa permitir promoção e rollback do mesmo software entre ambientes. O frontend usava uma URL pública configurável no build, o que criaria uma imagem diferente por ambiente. Também não existe plataforma autorizada para impor uma topologia proprietária.

## Decisão

Construir imagens OCI independentes para web e API, a partir de bases fixadas por digest, com processos sem root e label do SHA Git completo. O mesmo SHA será exposto no status da API e comparado pelo smoke antes de ampliar tráfego.

Em produção, o frontend usa caminhos relativos. Um gateway externo termina TLS e encaminha `/api/v1` para a API; as demais rotas seguem para o Next.js. URLs, certificados, credenciais e IDs de infraestrutura são injetados pela plataforma e não entram nas imagens.

O banco evolui por Flyway antes de readiness. Rollback troca apenas o artefato por outro já testado contra o schema atual; migrations não são revertidas automaticamente. Imagens são promovidas por digest do registry, nunca reconstruídas para staging ou produção.

## Alternativas

- compilar o frontend separadamente com `NEXT_PUBLIC_API_BASE_URL` para cada ambiente;
- servir o frontend dentro do Spring Boot;
- adotar Kubernetes, serviço serverless ou uma nuvem específica agora;
- usar tag mutável como `latest` para promoção;
- executar containers como root para simplificar permissões.

## Consequências

- o artefato promovido é verificável e independente do ambiente;
- gateway e aplicação ficam desacoplados de fornecedor;
- o gateway precisa preservar headers de encaminhamento e rotear `/api` corretamente;
- a publicação futura precisa registrar digest, assinatura/atestação e SBOM no registry escolhido;
- a validação local prova containers e perfil, mas não prova DNS, TLS, cofre, alertas ou backup externos.

## Riscos e mitigação

- release errado recebendo tráfego: smoke compara SHA esperado e status ativo;
- base alterada sem revisão: imagem base referenciada por digest;
- escrita no filesystem ou privilégio excessivo: filesystem read-only na validação, usuário não root, capabilities removidas;
- cookies quebrados por proxy: HTTPS público, headers `Forwarded` confiáveis apenas da plataforma e teste de cookie seguro;
- rollback incompatível: expand/contract, registro do schema e ensaio antes do rollout;
- rota `/api` ausente: contrato e smoke opcional pelo gateway público.

## Condições de revisão

Revisar ao escolher plataforma, registry, service mesh/CDN, estratégia multi-região, assinatura de imagens ou quando uma integração exigir egress controlado adicional.

## Aprovação

Proposto durante a preparação técnica da Fase 9. Aceitar somente após revisão do gate e autorização explícita da fase concluída.
