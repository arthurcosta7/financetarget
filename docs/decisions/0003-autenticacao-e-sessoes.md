# ADR 0003 — Autenticação local e sessões rotativas

- Estado: Proposto
- Data: 27/08/2026

## Contexto

O produto web precisa de e-mail/senha e proteção de dados financeiros. Tokens acessíveis ao JavaScript aumentam o impacto de XSS.

## Decisão

Usar Spring Security, senha com hash forte, access token curto em cookie HttpOnly e refresh token opaco rotativo armazenado em hash. Preparar porta para OIDC futuro. Aplicar CSRF e autorização por espaço/recurso.

## Alternativas

- tokens em localStorage;
- sessão tradicional única no banco;
- provedor OIDC obrigatório desde o primeiro dia.

## Consequências

- Reduz exposição de token ao JavaScript.
- Exige CSRF, rotação, família de sessão e revogação corretas.
- Autenticação local aumenta responsabilidade de segurança; revisão poderá favorecer IdP gerenciado antes do beta.

## Revisão

Reavaliar na Fase 2 após comparar risco, custo e dependência de um IdP gerenciado.
