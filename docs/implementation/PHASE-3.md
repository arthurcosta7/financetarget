# Fase 3 — identidade e onboarding

**Estado:** concluída tecnicamente em 27/08/2026; aguardando aprovação do usuário.

## Objetivo

Entregar uma fatia vertical segura para criação e acesso à conta, coleta do perfil financeiro mínimo e exercício inicial dos direitos de privacidade. A fase deve demonstrar isolamento entre usuários e comportamento seguro nos fluxos negativos antes de qualquer regra de metas.

## Escopo

- cadastro com e-mail, nome e senha;
- verificação de e-mail com token descartável e criação do espaço pessoal;
- login, sessão curta, renovação rotativa e logout;
- recuperação de acesso sem enumeração de contas;
- perfil financeiro mínimo calculado exclusivamente no backend;
- consentimentos versionados e auditáveis;
- consulta e edição do perfil da conta;
- exportação dos dados próprios após reautenticação;
- solicitação idempotente de exclusão, sem apagar dados nesta fase;
- adaptador de mensagens apenas para desenvolvimento e testes;
- telas responsivas e acessíveis para cadastro, acesso, onboarding e conta;
- contrato OpenAPI, migrations e testes de segurança e isolamento.

## Fora de escopo

- metas, Goal Engine e Scenario Engine executáveis;
- convites e colaboração do casal;
- envio real de e-mail, SMS ou push;
- Open Finance, pagamentos ou qualquer provedor externo;
- conteúdo jurídico definitivo;
- execução física da exclusão;
- deploy, staging ou produção.

## Critérios de aceite

| Critério | Evidência esperada |
|---|---|
| Conta verificada | token de uso único ativa a conta e cria um único espaço pessoal |
| Respostas neutras | cadastro duplicado e recuperação não revelam a existência da conta |
| Senha protegida | Argon2id, política sem regras arbitrárias e nenhum segredo exposto |
| Sessão segura | cookies HttpOnly, CSRF, acesso curto, refresh rotativo e revogação |
| Isolamento | usuário não lê nem altera perfil, exportação ou solicitação de outro usuário |
| Dinheiro exato | valores monetários persistidos como `numeric`/`BigDecimal`; cálculo só no backend |
| Consentimento | propósito e versão registrados de forma append-only |
| Privacidade | exportação exclui segredos e dados alheios; exclusão é idempotente e consultável |
| UX | fluxos operáveis por teclado, mensagens claras, reflow a 320 px e temas invertíveis |
| Contrato | OpenAPI e tipos TypeScript regenerados sem divergência |

## Assunções operacionais

- até existir provedor real, verificação e recuperação usam uma caixa de saída efêmera restrita ao perfil `dev`; testes usam uma porta capturável em memória;
- a sugestão inicial de capacidade é `max(renda recorrente - despesas essenciais, 0)`, apresentada como estimativa editável, nunca como recomendação ou garantia;
- a exclusão apenas registra uma solicitação. Regras sobre espaços compartilhados continuam abertas e impedem a remoção física nesta fase;
- controles distribuídos de abuso e entrega real de mensagens serão revisitados antes do beta.

## Gate

A fase termina somente após testes demonstrarem autorização fechada, isolamento por usuário, rotação/reuso de sessão, uso único de tokens e respostas genéricas. Depois disso, o projeto deve parar e aguardar aprovação explícita para a Fase 4.

Os critérios foram demonstrados em `docs/testing/PHASE-3-RESULTS.md`. A Fase 4 não foi iniciada.
