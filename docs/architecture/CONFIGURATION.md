# Configuração, ambientes e feature flags

## Princípio

Configuração muda entre ambientes ou precisa ser operada sem recompilar. Invariantes do domínio permanecem em código e testes. Não criar um sistema genérico de configuração para tudo.

## Ambientes

| Ambiente | Dados | Integrações | Segredos | Objetivo |
|---|---|---|---|---|
| `dev` | sintéticos e resetáveis | mocks locais | credenciais locais sem valor externo | desenvolvimento |
| `test` | efêmeros e determinísticos | fakes/Testcontainers | injetados pelo runner | testes |
| `staging` | sintéticos representativos | sandboxes autorizados | isolados | validação pré-release |
| `production` | reais minimizados | provedores aprovados | cofre gerenciado | serviço real |

Nunca copiar produção integralmente para dev ou staging.

## Categorias configuráveis

- conexão e pool de banco;
- origem permitida e cookies;
- URLs públicas por ambiente;
- timeouts, retries e limites;
- retenções aprovadas;
- templates e remetentes;
- IDs de plano e provedor;
- thresholds operacionais;
- flags de integração;
- parâmetros de presets financeiros versionados.

## Proibido hardcodar

- segredos e tokens;
- URLs externas;
- IDs comerciais;
- preços e limites de plano;
- inflação, retorno, CET ou câmbio;
- moeda ou timezone implícito;
- textos jurídicos;
- e-mails e telefones;
- feature flags;
- credenciais e nomes de bucket.

## Validação

- configuração obrigatória falha no startup com mensagem sem segredo;
- tipos e intervalos são validados;
- ambiente de produção rejeita defaults inseguros;
- `.env.example` contém apenas nomes e exemplos não secretos;
- configuração efetiva pode ser auditada com valores sensíveis mascarados.

### Implementação da Fase 7

`EnvironmentSafetyGuard` aceita somente `dev`, `test`, `staging` ou `production`. Em staging/produção, o processo não inicia com cookies inseguros, origem local/não HTTPS ou qualquer flag de integração ativa. O profile `staging` carrega apenas migrations base, graceful shutdown, logs ECS e sampling configurável. `.env.staging.example` contém marcadores, nunca credenciais funcionais.

### Endurecimento da Fase 9

Produção também exige `APP_EXPECTED_RELEASE_ID` no formato de SHA Git completo e igual à revisão imutável gravada no JAR, versões jurídicas não provisórias e o adaptador transacional de identidade configurado. O perfil `production` mantém shutdown gracioso, logs ECS e histogramas HTTP. `.env.production.example` é somente um contrato com marcadores; nenhum valor pode ser promovido diretamente dele.

## Segredos

- nunca versionados;
- separados por ambiente;
- acesso por privilégio mínimo;
- rotação documentada;
- não aparecem em logs, erros ou métricas;
- desenvolvimento usa valores locais descartáveis.

## Feature flags

Flags possuem proprietário, finalidade, valor padrão seguro, ambientes, data de revisão e plano de remoção. Flags de segurança falham fechadas quando aplicável. Flags não substituem autorização.

| Flag | Proprietário | Padrão | Finalidade | Revisão |
|---|---|---:|---|---|
| `payments-mock` | Produto + plataforma | `false` | Exercitar checkout e webhook sem cobrança | antes da Fase 7 |
| `notifications-mock` | Produto + privacidade | `false` | Registrar intenção sem envio | antes da Fase 7 |
| `open-finance` | Produto + segurança | `false` | Reservada para futuro ADR | antes de sandbox |
| `loyalty` | Produto | `false` | Reservada para futuro ADR | antes de sandbox |
| `travel` | Produto | `false` | Reservada para futuro ADR | antes de sandbox |
| `real-estate-financing` | Produto + jurídico | `false` | Reservada para futuro ADR | antes de sandbox |
| `auto-financing` | Produto + jurídico | `false` | Reservada para futuro ADR | antes de sandbox |

### Mensagens transacionais de identidade

O Resend é um adaptador específico de `IdentityMessagePort` e permanece desligado por padrão. Sua configuração usa `APP_INTEGRATION_RESEND_ENABLED`, `RESEND_API_KEY`, `APP_RESEND_ENDPOINT`, `APP_EMAIL_FROM_ADDRESS`, `APP_EMAIL_FROM_NAME`, `APP_PUBLIC_WEB_URL`, `APP_RESEND_CONNECT_TIMEOUT` e `APP_RESEND_READ_TIMEOUT`.

Quando habilitado, chave, remetente, URLs e timeouts são validados no startup. Staging e produção exigem URL pública HTTPS. A chave nunca é exposta ao frontend. Endpoint e remetente não são constantes de domínio e podem mudar sem recompilar.

As flags de integração futura devem ser removidas caso a hipótese seja descartada; habilitá-las sem adaptador não produz conexão nem acesso.

## Presets de cenário

Rótulos como conservador ou otimista não carregam taxas fixas no código. Um conjunto versionado e visível define os valores; o usuário confirma ou ajusta. A origem e a data do preset são registradas.
