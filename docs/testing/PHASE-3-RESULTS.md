# Resultados de validação da Fase 3

- Data: 27/08/2026
- Resultado: aprovado tecnicamente; aguardando aprovação do gate pelo usuário.

## Backend

`mvnw verify` executou com Java 25, Spring Boot 3.5.16 e PostgreSQL 17.11 efêmero.

- 9 testes de integração aprovados;
- migrations `V1` e `V2` aplicadas do zero;
- cadastro protegido por CSRF e resposta neutra para duplicidade;
- verificação de uso único e um espaço pessoal por conta verificada;
- cookies HttpOnly/SameSite e bootstrap CSRF real;
- refresh rotativo e revogação da família após reuso;
- recuperação de uso único e revogação das sessões anteriores;
- perfil monetário exato com `numeric(19,2)` e `BigDecimal`;
- isolamento entre duas contas na leitura do perfil e na exportação;
- exportação sem senha, hash ou token;
- solicitação de exclusão idempotente.

## Frontend

- OpenAPI regenerado sem erro;
- ESLint sem avisos;
- TypeScript estrito aprovado;
- 6 testes Vitest aprovados;
- build de produção do Next.js aprovado;
- rotas dinâmicas com CSP por nonce e `strict-dynamic`;
- cadastro, login, verificação, recuperação, onboarding e conta implementados;
- renovação transparente de sessão nas chamadas autenticadas.

## Inspeção visual e acessibilidade

O cadastro foi inspecionado no navegador real em 1440 × 900 e 320 × 800, nos temas claro e escuro.

- nenhum overflow horizontal (`scrollWidth` igual a `clientWidth`);
- landmarks, heading principal, labels e skip link expostos semanticamente;
- foco visível no seletor de tema;
- nenhum erro ou aviso de console;
- CSP presente com nonce, origem da API configurada e fontes externas bloqueadas;
- hierarquia, contraste e reflow coerentes com a direção “precisão calma”.

## Revisões estáticas

- nenhum `float` ou `double` para dinheiro;
- nenhum token em `localStorage` ou `sessionStorage`;
- nenhum SDK ou URL de provedor no domínio;
- moeda, origem da API e versões documentais configuráveis por ambiente;
- `git diff --check` sem erro de whitespace.

## Limitações conhecidas

- rate limit é local à instância e precisa de armazenamento compartilhado antes de escalar horizontalmente;
- verificação de senha comprometida ainda não possui fonte externa aprovada;
- entrega de verificação e recuperação é apenas simulada em `dev` e testes;
- exclusão física permanece bloqueada pelas regras abertas de espaços compartilhados;
- ADR 0009 aguarda aceite no gate da fase.
