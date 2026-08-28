# FinanceTarget

SaaS brasileiro de planejamento financeiro orientado a metas. O produto transforma objetivos de vida em planos compreensíveis, com cálculos transparentes e arquitetura preparada para integrações sem acoplamento direto a fornecedores.

## Estado atual

A Fase 7 está tecnicamente concluída e aguarda aprovação. O projeto já possui identidade, onboarding financeiro, privacidade inicial, metas, Goal Engine e Scenario Engine, dashboard, assinaturas e notificações simuladas, staging fail-closed, observabilidade, auditoria de dependências e recuperação testada. O Resend está disponível como integração real e opcional apenas para mensagens de identidade. Colaboração operacional, pagamentos reais, beta e deploy externo ainda não foram implementados.

## Stack

- frontend: Next.js 16, React 19 e TypeScript 5.9;
- backend: Spring Boot 3.5 e Java 25;
- build do backend: Maven 3.9 pelo Maven Wrapper do repositório;
- banco: PostgreSQL 17 e Flyway;
- testes: Vitest, Spring Boot Test e Testcontainers;
- ambiente local: Docker Compose e PowerShell.

O backend usa exclusivamente Maven. Não é necessário instalar Maven globalmente e não há configuração Gradle.

## Pré-requisitos

Instale e deixe disponíveis no terminal:

- [Git](https://git-scm.com/downloads);
- [Node.js 24](https://nodejs.org/);
- [Java JDK 25](https://adoptium.net/);
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) em execução;
- PowerShell 7 ou o Windows PowerShell.

O Corepack incluído no Node.js seleciona automaticamente o pnpm 11.19 definido em `package.json`; não é necessário instalar pnpm globalmente. As demais versões esperadas também estão registradas em `.nvmrc`, `.java-version` e no Maven Wrapper.

## Primeira execução no Windows

Abra o PowerShell na raiz do repositório e execute:

```powershell
Copy-Item .env.example .env
corepack pnpm install --frozen-lockfile
```

O `.env` contém apenas configuração local e está ignorado pelo Git. Não coloque credenciais, dados pessoais ou dados financeiros reais nele.

Depois, use três terminais PowerShell na raiz do projeto.

### Terminal 1 — PostgreSQL

```powershell
.\scripts\dev.ps1
```

O script inicia o PostgreSQL com Docker Compose e aguarda o banco ficar saudável. No exemplo, o banco fica disponível na porta `55432`.

### Terminal 2 — backend com Maven

```powershell
.\scripts\run-api.ps1
```

Esse é o comando recomendado para desenvolvimento. Ele carrega o `.env`, entra em `apps/api` e executa:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

O wrapper baixa automaticamente a versão correta do Maven na primeira execução.

### Terminal 3 — frontend

```powershell
.\scripts\run-web.ps1
```

Abra [http://localhost:3000/cadastro](http://localhost:3000/cadastro) quando os três serviços estiverem prontos.

## Endereços locais

| Serviço | Endereço |
|---|---|
| Site | [http://localhost:3000](http://localhost:3000) |
| Cadastro | [http://localhost:3000/cadastro](http://localhost:3000/cadastro) |
| Login | [http://localhost:3000/entrar](http://localhost:3000/entrar) |
| Status da API | [http://localhost:8080/api/v1/system/status](http://localhost:8080/api/v1/system/status) |
| Health check | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |
| Contrato OpenAPI | [http://localhost:8080/openapi.yaml](http://localhost:8080/openapi.yaml) |

## Verificação de conta no ambiente local

Por padrão, o perfil `dev` não envia e-mails reais. Após cadastrar uma conta com dados exclusivamente sintéticos, consulte no navegador ou no PowerShell:

```powershell
$Email = [uri]::EscapeDataString('teste@example.invalid')
Invoke-RestMethod "http://localhost:8080/api/v1/dev/identity-messages/latest?email=$Email"
```

A resposta contém `kind`, `token` e `capturedAt`. Copie o `token` para a tela [http://localhost:3000/verificar-email](http://localhost:3000/verificar-email). O mesmo endpoint retorna o token mais recente de recuperação depois que ela for solicitada pela interface.

Esse recurso existe somente no perfil `dev` e mantém mensagens apenas em memória.

Para usar Resend, configure no `.env` pelo menos `APP_INTEGRATION_RESEND_ENABLED=true`, `RESEND_API_KEY`, `APP_EMAIL_FROM_ADDRESS` e `APP_PUBLIC_WEB_URL`, e reinicie a API. Quando o Resend está habilitado, a caixa em memória e o endpoint `/api/v1/dev/identity-messages` deixam de existir. Nunca use uma chave real no `.env.example`, no frontend ou no Git.

Uma conta ainda pendente pode solicitar outro link em [http://localhost:3000/reenviar-verificacao](http://localhost:3000/reenviar-verificacao). O token anterior é invalidado e a resposta não revela se a conta existe ou qual é seu estado.

## Comandos do backend Maven

Para executar o backend manualmente, carregue primeiro as variáveis do projeto:

```powershell
. .\scripts\load-env.ps1 -Path .\.env
Set-Location .\apps\api
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Para validar ou empacotar o backend:

```powershell
Set-Location .\apps\api
.\mvnw.cmd verify
.\mvnw.cmd package -DskipTests
```

Os testes de integração usam um PostgreSQL efêmero com Testcontainers, portanto o Docker Desktop precisa estar em execução.

## Validação completa

Na raiz do projeto:

```powershell
.\scripts\check.ps1
```

O script usa `corepack pnpm` para regenerar os tipos a partir do OpenAPI, executar lint, verificação TypeScript, testes e build do frontend, e então roda `mvnw.cmd verify` no backend.

Para os gates de segurança e observabilidade da Fase 7:

```powershell
.\scripts\security-audit.ps1
.\scripts\validate-observability.ps1
```

Ambos usam Docker para ferramentas fixadas por digest. O primeiro também gera o SBOM Maven.

Se um banco `dev` muito antigo acusar checksum de migration, não execute `flyway repair` automaticamente. Preserve o volume e consulte [Operação de staging](docs/operations/STAGING.md) e [Backup e restauração](docs/operations/BACKUP-RESTORE.md); ambientes sintéticos descartáveis devem ser recriados a partir das migrations versionadas.

## Encerramento

Interrompa frontend e backend com `Ctrl+C`. Para parar o banco sem apagar o volume local:

```powershell
docker compose --env-file .env stop postgres
```

A remoção do volume não faz parte do fluxo normal de desenvolvimento.

## Estrutura principal

```text
apps/
  api/        Backend Spring Boot construído com Maven
  web/        Frontend Next.js
docs/         Produto, arquitetura, segurança, UX, decisões e testes
scripts/      Inicialização e validação no Windows
compose.yaml  PostgreSQL local
```

## Documentação

- [Desenvolvimento local detalhado](docs/DEVELOPMENT.md)
- [Estado do projeto](docs/STATUS.md)
- [PRD](docs/product/PRD.md)
- [Roadmap](docs/product/ROADMAP.md)
- [Arquitetura](docs/architecture/OVERVIEW.md)
- [Autenticação](docs/security/AUTHENTICATION.md)
- [Threat model](docs/security/THREAT-MODEL.md)
- [Registro de decisões](docs/decisions/README.md)
- [Escopo da Fase 7](docs/implementation/PHASE-7.md)
- [Resultados da Fase 7](docs/testing/PHASE-7-RESULTS.md)
- [Operação de staging](docs/operations/STAGING.md)
- [Resposta a incidentes](docs/operations/INCIDENT-RESPONSE.md)
- [Backup e restauração](docs/operations/BACKUP-RESTORE.md)
- [Release e rollback](docs/operations/RELEASE-ROLLBACK.md)

## Regra de evolução

O desenvolvimento é conduzido por fases. Uma fase só avança após revisão dos entregáveis, validações aplicáveis, atualização da documentação e aprovação explícita. A Fase 8 ainda não está autorizada.
