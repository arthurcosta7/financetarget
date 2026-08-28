# Desenvolvimento local

## Pré-requisitos

- Node.js 24;
- Corepack, incluído no Node.js, para fornecer pnpm 11.19;
- Java 25;
- Docker Desktop em execução.

O Maven 3.9.16 é baixado automaticamente pelo wrapper do projeto.

## Primeira execução no Windows

1. Copie `.env.example` para `.env` e mantenha os valores apenas locais.
2. Execute `corepack pnpm install --frozen-lockfile`.
3. Execute `.\scripts\dev.ps1` para iniciar e aguardar o PostgreSQL.
4. Em outro terminal, execute `.\scripts\run-api.ps1`.
5. Em outro terminal, execute `.\scripts\run-web.ps1`.
6. Abra `http://localhost:3000/cadastro`.

O banco do projeto usa a porta local configurável `55432` no exemplo para não disputar a instalação PostgreSQL já existente nesta máquina. Alterar a porta exige somente atualizar `.env`.

## Contrato da API

O arquivo fonte é `apps/api/src/main/resources/static/openapi.yaml`. Após qualquer mudança compatível com a fase autorizada, execute `pnpm generate:api` e versione o tipo gerado. A interface não deve duplicar manualmente os contratos.

## Configuração

Variáveis obrigatórias falham no startup quando ausentes. Dev, staging e produção usam o mesmo artefato e valores injetados por ambiente. Segredos reais não pertencem a `.env`, ao repositório, a imagens ou a logs.

O profile `dev` acrescenta o seed sintético e uma caixa efêmera de mensagens de identidade. Após cadastrar um e-mail exclusivamente sintético, consulte `GET /api/v1/dev/identity-messages/latest?email=...` para obter o código de verificação. O mesmo fluxo vale para recuperação. Esse endpoint não existe fora de `dev`, não integra provedor e não deve receber dados reais.

Moeda inicial, fuso de negócio, origem CORS/API, nomes e durações dos cookies e versões dos documentos são configuração de ambiente. `APP_AUTH_SECURE_COOKIES` só pode ser `false` no desenvolvimento HTTP local. Staging e produção não foram provisionados.

Os hubs simulados da Fase 6 permanecem desligados por padrão. No profile `dev`, habilite somente `APP_FEATURE_PAYMENTS_MOCK` e `APP_FEATURE_NOTIFICATIONS_MOCK` quando precisar testar a jornada local. O segredo `APP_MOCK_PAYMENT_WEBHOOK_SECRET` deve ser longo, sintético e exclusivo do ambiente; o mock não cobra, não envia mensagens e não é um adaptador de produção. As flags de Open Finance, fidelidade, viagens e financiamentos apenas expõem disponibilidade planejada e não habilitam integrações.

Testes usam PostgreSQL efêmero e mensagens capturadas em memória. Tokens e senhas não são registrados em logs.

## Encerramento local

Interrompa API e web com `Ctrl+C`. Para parar o banco sem apagar os dados locais, execute `docker compose stop`. A remoção do volume não faz parte do fluxo normal.
