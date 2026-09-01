# Resultados da Fase 9

## Escopo validado

A validação cobriu somente a preparação técnica local de produção. Usou imagens locais, banco efêmero, credenciais sintéticas e domínios `.test`; não publicou artefatos, criou infraestrutura, enviou e-mail, abriu tráfego ou tratou dados reais.

## Evidências automatizadas

Executadas em 31/08/2026:

- `mvnw verify`: 38 testes backend, zero falhas, migrations V1–V7 em PostgreSQL 17.11 efêmero e JAR empacotado;
- frontend: lint sem warnings, TypeScript estrito, 17 testes em 8 arquivos e build Next.js com 18 rotas;
- OpenAPI 0.8 regenerado e tipos versionados consistentes;
- testes do guard aceitaram somente a configuração completa de produção e rejeitaram SHA inválido, documento provisório ou Resend desligado;
- `security-audit.ps1`: pnpm audit e OSV sobre 82 componentes Maven e 545 pacotes do lockfile sem vulnerabilidades conhecidas;
- cinco regras Prometheus aprovadas pelo `promtool`;
- `git diff --check` e `docker compose config --quiet` sem erros.

## Prova dos artefatos

`build-release.ps1` construiu imagens API e web a partir de bases fixadas por digest. A inspeção confirmou usuário não root e o mesmo label OCI de revisão nas duas imagens.

`validate-release.ps1` executou as imagens com:

- perfil `production` e filesystem read-only para web/API;
- capabilities removidas e `no-new-privileges`;
- PostgreSQL efêmero em rede interna;
- Flyway até schema `7` antes da readiness;
- `/api/v1/system/status` com o SHA esperado;
- cookie CSRF `Secure` e `SameSite=Lax`;
- CSP, HSTS e `X-Content-Type-Options` no frontend;
- teardown automático da topologia e do volume sintético.

## Validação remota

O commit `fc7082625fc7fac88d43a5eb2ec5cafae145cf2f` foi publicado em `main` e validado pela [execução 33462061462 do GitHub Actions](https://github.com/arthurcosta7/financetarget/actions/runs/33462061462) em 31/08/2026. Os quatro jobs — Dependências, API, Web e Artefatos de release — foram concluídos com sucesso, incluindo build e smoke do perfil de produção em runner Linux.

A primeira execução revelou que `apps/api/mvnw` estava versionado sem o bit executável. A correção preservou o conteúdo do wrapper, alterou apenas seu modo para `100755` e atualizou as Actions oficiais para versões sem runtime depreciado, fixadas por SHA imutável.

## Limites da evidência

- a rota same-origin `/api/v1` foi especificada, mas não pode ser provada sem o gateway externo escolhido;
- scan/assinatura e digests de registry dependem do registry autorizado;
- TLS, DNS, cofre, collector, Alertmanager, backup imutável e restauração externa não existem;
- os gates jurídico, RIPD, retenção, responsáveis e validação matemática não são substituídos por esta evidência.

## Veredito

A fatia técnica local da Fase 9 atende aos critérios definidos. O lançamento e a conclusão integral da fase permanecem bloqueados pelos gates humanos e de infraestrutura listados em `docs/implementation/PHASE-9.md`.
