# ADR 0009 — Tokens opacos e entrega simulada na Fase 3

- Estado: Proposto
- Data: 27/08/2026

## Contexto

O ADR 0003 exige acesso curto em cookie HttpOnly, refresh opaco rotativo, CSRF e revogação. Na primeira implementação, um token de acesso autoportante adicionaria gestão criptográfica e dificultaria revogação imediata. Também não há autorização para enviar e-mails reais.

## Decisão

Usar valores aleatórios opacos para acesso, renovação, verificação e recuperação. Persistir somente hashes SHA-256. O acesso terá duração curta; o refresh será de uso único, rotativo e vinculado a uma família revogável. Autenticação consultará o banco e cookies serão HttpOnly, `SameSite=Lax` e `Secure` por configuração.

Usar Argon2id para senhas e proteção CSRF por cookie legível pelo frontend e cabeçalho explícito. Mensagens de verificação e recuperação passarão por uma porta do domínio; na Fase 3 haverá somente adaptador efêmero no perfil `dev` e capturável em testes, sem logar tokens.

## Alternativas

- JWT assinado para acesso e refresh opaco;
- sessão única tradicional sem rotação;
- provedor de identidade e e-mail reais desde esta fase;
- devolver tokens no corpo das respostas de produção.

## Consequências

- revogação e mudança de senha produzem efeito imediato;
- segredos de sessão nunca ficam acessíveis ao JavaScript;
- cada requisição autenticada faz consulta ao banco;
- a caixa de saída de desenvolvimento não existe fora do perfil `dev`;
- a estratégia pode migrar para IdP ou token autoportante atrás das mesmas portas.

## Riscos e mitigação

- crescimento do volume de consultas: índices por hash e revisão com métricas antes do beta;
- abuso distribuído: limite local é apenas defesa inicial e deverá ser substituído por controle compartilhado antes de escalar horizontalmente;
- exposição acidental no ambiente de desenvolvimento: endpoint de inspeção restrito ao perfil `dev`, com dados sintéticos e sem logs de tokens.

## Condições de revisão

Reavaliar antes do beta, ao introduzir múltiplas instâncias, IdP gerenciado ou provedor real de mensagens.
