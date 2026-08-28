# Operação de staging

## Escopo atual

O projeto possui um perfil `staging` reproduzível, mas nenhum ambiente externo foi criado. Plataforma, domínio, TLS, cofre, coletor de métricas e responsáveis operacionais ainda precisam ser escolhidos e autorizados.

Staging aceita somente dados sintéticos, usa apenas migrations base e mantém todas as integrações desligadas. O startup falha se cookies não forem seguros, se CORS não usar origens HTTPS exatas ou se qualquer flag de integração estiver ativa.

## Preparação

1. Provisione um PostgreSQL vazio e isolado; não reutilize o banco `dev`.
2. Copie `.env.staging.example` para um arquivo ignorado pelo Git.
3. Injete banco, versões jurídicas provisórias e chave de backup pelo runner ou cofre.
4. Mantenha `APP_ENV=staging`, `APP_AUTH_SECURE_COOKIES=true` e todas as flags `false`.
5. Inicie a API com `./scripts/run-api-staging.ps1 -EnvironmentFile <arquivo>`.
6. Inicie o build de produção do frontend apontando para a URL sintética da API.

Não execute `.env.staging.example` diretamente: os valores `inject-from-secret-store` são marcadores deliberados.

## Verificação pré-release

```powershell
./scripts/staging-smoke.ps1 `
  -ApiBaseUrl https://api.staging.example.test `
  -WebBaseUrl https://staging.example.test
```

O smoke exige readiness `UP`, schema esperado, correlação de requisição, métricas HTTP, CSP, HSTS e `nosniff`.

Para carga sintética curta:

```powershell
$env:API_BASE_URL='https://api.staging.example.test'
node ./scripts/load-smoke.mjs
```

O padrão usa 8 workers por 10 segundos, rejeita qualquer erro e exige p95 até 500 ms. Isso é um teste de fumaça, não dimensionamento de produção.

## Restrições

- Não copiar dados de produção.
- Não habilitar mocks nem adaptadores reais em staging sem ADR e autorização.
- Não expor `/actuator/prometheus` diretamente à internet; o acesso atual é somente loopback.
- Não usar `flyway repair` para contornar checksum. Investigue a origem e restaure/recrie o ambiente sintético.
- Não promover um artefato cujo SBOM ou auditoria esteja bloqueante.

## Saída do ambiente

Encerrar processos, destruir somente recursos efêmeros identificados, preservar logs técnicos minimizados e registrar o resultado em `docs/testing/`.
