# Autenticação e autorização

## Decisão aprovada

Autenticação local no backend com Spring Security para e-mail e senha, mantendo uma fronteira que permita OIDC futuro. A identidade financeira e as permissões do espaço continuam no produto, independentemente do mecanismo de login.

## Sessões web

- Access token de curta duração em cookie `HttpOnly`, `Secure` em produção e prefixo `__Host-` quando aplicável.
- Refresh token opaco, aleatório, armazenado somente em hash no banco.
- Rotação a cada uso e detecção de reutilização por família de sessão.
- Revogação no logout, troca de senha, recuperação, suspeita de abuso ou ação do usuário.
- Cookies nunca acessíveis por JavaScript.
- Nenhum token em `localStorage` ou `sessionStorage`.
- Proteção CSRF para requisições mutáveis autenticadas por cookie.
- CORS com allowlist exata por ambiente.

`SameSite=Lax` é a proposta inicial para permitir navegação normal e callbacks controlados; o valor será validado com o fluxo real. Cookies sensíveis não usam domínio amplo.

## Implementação da Fase 3

- acesso curto e refresh são opacos, aleatórios e persistidos somente como SHA-256;
- refresh é de uso único e a reutilização revoga a família dentro da transação;
- acesso, refresh, verificação e recuperação possuem durações configuráveis;
- senha usa Argon2id com salt aleatório e aceita de 15 a 128 caracteres por configuração;
- cookie de acesso usa caminho `/`; refresh fica restrito a `/api/v1/auth`;
- CSRF usa cookie `XSRF-TOKEN`, cabeçalho explícito e tratamento próprio para SPA;
- CSP do frontend usa nonce por requisição, `strict-dynamic` e origem da API configurada;
- respostas de cadastro duplicado e recuperação são neutras;
- rate limit usa identificador normalizado em hash e janela configurável persistida no PostgreSQL, compartilhada entre instâncias;
- exportação e exclusão exigem nova confirmação da senha;
- nenhum adaptador de mensagem real está habilitado.

Detalhes e alternativas estão no ADR 0009, aceito após o gate da Fase 3.

Na Fase 7, a janela local foi substituída por `authentication_attempt_window`. A chave persistida é SHA-256 de ação e identificador normalizado, possui expiração e não contém o e-mail. O incremento usa transação independente para que uma falha de login não reverta a contagem.

## Senhas

- Hash com Argon2id ou implementação equivalente aprovada na Fase 2.
- Parâmetros medidos no ambiente de produção e versionados.
- Sem armazenamento reversível.
- Permitir senhas longas e gerenciadores de senha.
- Bloquear senhas sabidamente comprometidas quando possível sem expor a senha.
- Não impor trocas periódicas arbitrárias.
- Comparação em tempo constante onde aplicável.

## Fluxos

### Cadastro

1. Normalizar e validar e-mail.
2. Responder sem expor detalhes indevidos.
3. Criar conta pendente.
4. Emitir token de verificação aleatório, de uso único e armazenado em hash.
5. Verificar e criar espaço pessoal.
6. Registrar auditoria mínima.

### Recuperação

- Resposta pública idêntica para conta existente ou inexistente.
- Token curto, de uso único, armazenado em hash.
- Revogar sessões existentes após conclusão.
- Notificar a pessoa sobre a mudança por canal verificado.

### Refresh

- Rotacionar token dentro de transação.
- Reutilização de token revogado invalida a família inteira.
- Limitar sessões por usuário de forma configurável.

## OIDC futuro

Usar Authorization Code com PKCE. O adaptador converte a identidade externa em uma identidade canônica. Vincular contas exige reautenticação e prevenção contra account linking indevido.

## Autorização

A autorização possui duas camadas:

1. permissão de aplicação, baseada no papel dentro do `PlanningSpace`;
2. ownership/escopo do recurso, verificando que meta, cenário e contribuição pertencem ao espaço autorizado.

Não confiar apenas em papéis globais ou em IDs enviados pelo cliente.

| Ação | Owner | Editor | Viewer |
|---|---:|---:|---:|
| Consultar espaço e metas | Sim | Sim | Sim |
| Criar/editar meta | Sim | Sim | Não |
| Registrar progresso | Sim | Sim | Não |
| Criar cenário | Sim | Sim | Não |
| Convidar membro | Sim | Não | Não |
| Alterar papel | Sim | Não | Não |
| Remover membro | Sim | Não | Não |
| Arquivar/excluir espaço | Sim, com controles adicionais | Não | Não |

Alterações sensíveis exigem reautenticação recente. A remoção do último proprietário é proibida.

## Controles contra abuso

- rate limit por IP, conta, sessão e ação;
- backoff gradual sem lockout permanente explorável;
- mensagens anti-enumeração;
- alertas de nova sessão e mudança de credencial;
- trilha de login, falha, recuperação e revogação sem tokens ou senha;
- MFA preparado para fase posterior, priorizando WebAuthn/passkeys.

## Testes obrigatórios futuros

- expiração e rotação;
- reutilização de refresh token;
- CSRF;
- CORS;
- enumeração;
- troca de IDs entre espaços;
- papéis de casal;
- convite expirado, reutilizado ou destinado a outro e-mail;
- concorrência ao alterar papel;
- revogação após troca de senha;
- cookies e headers no ambiente de produção.
