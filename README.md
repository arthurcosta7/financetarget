# FinanceTarget

SaaS brasileiro de planejamento financeiro orientado a metas. O produto transforma objetivos de vida em planos compreensíveis, com cálculos transparentes e arquitetura preparada para integrações sem acoplamento direto a fornecedores.

## Estado atual

A Fase 4 está concluída e a Fase 5 foi autorizada. O projeto já possui identidade, onboarding financeiro, privacidade inicial, primeira jornada de meta, Goal Engine puro, snapshots imutáveis, contribuições manuais, frontend responsivo, API e PostgreSQL. Scenario Engine, colaboração operacional, integrações reais, pagamentos e deploy ainda não foram implementados.

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

O perfil `dev` não envia e-mails reais. Após cadastrar uma conta com dados exclusivamente sintéticos, consulte no navegador ou no PowerShell:

```powershell
$Email = [uri]::EscapeDataString('teste@example.invalid')
Invoke-RestMethod "http://localhost:8080/api/v1/dev/identity-messages/latest?email=$Email"
```

A resposta contém `kind`, `token` e `capturedAt`. Copie o `token` para a tela [http://localhost:3000/verificar-email](http://localhost:3000/verificar-email). O mesmo endpoint retorna o token mais recente de recuperação depois que ela for solicitada pela interface.

Esse recurso existe somente no perfil `dev`, mantém mensagens apenas em memória e não substitui uma integração real de e-mail.

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
- [Escopo da Fase 3](docs/implementation/PHASE-3.md)
- [Resultados de validação da Fase 3](docs/testing/PHASE-3-RESULTS.md)

## Regra de evolução

O desenvolvimento é conduzido por fases. Uma fase só avança após revisão dos entregáveis, validações aplicáveis, atualização da documentação e aprovação explícita. A Fase 4 ainda não está autorizada.
