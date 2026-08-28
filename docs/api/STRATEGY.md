# Estratégia de API

## Princípios

- REST sob `/api/v1`.
- OpenAPI é o contrato publicável e a base do cliente TypeScript.
- DTOs externos são separados do domínio.
- Erros usam Problem Details com código de domínio estável.
- IDs públicos não são sequenciais.
- Toda consulta de recurso valida associação ao espaço.
- Campos desconhecidos são rejeitados nos comandos críticos.
- Datas e dinheiro possuem formatos documentados.

## Envelope de dinheiro

```json
{
  "amount": "1250.00",
  "currency": "BRL"
}
```

Valores decimais via JSON são strings para preservar precisão entre JavaScript, Java e PostgreSQL.

## Recursos iniciais

Identidade, perfil, privacidade e os endpoints de metas e contribuições abaixo já estão implementados. Cenários, snapshots consultáveis e preview permanecem planejados para evolução incremental.

```text
/api/v1/auth/*
/api/v1/me
/api/v1/planning-spaces
/api/v1/planning-spaces/{spaceId}/members
/api/v1/planning-spaces/{spaceId}/invitations
/api/v1/invitations/{token}/accept
/api/v1/planning-spaces/{spaceId}/financial-profile
/api/v1/planning-spaces/{spaceId}/goals
/api/v1/planning-spaces/{spaceId}/goals/{goalId}
/api/v1/planning-spaces/{spaceId}/goals/{goalId}/contributions
/api/v1/planning-spaces/{spaceId}/goals/{goalId}/scenarios
/api/v1/planning-spaces/{spaceId}/goals/{goalId}/snapshots
/api/v1/goal-projections/preview
/api/v1/privacy/exports
/api/v1/privacy/deletion-requests
```

`preview` calcula sem persistir uma meta, mas aplica validação, limites e autenticação apropriados. Persistir um cenário gera snapshot imutável.

## Concorrência

Recursos editáveis expõem `version` ou ETag. Atualização com versão obsoleta retorna conflito e não sobrescreve o trabalho do parceiro.

## Idempotência

Obrigatória inicialmente em:

- criação de contribuição;
- criação/aceite de convite;
- solicitações de exportação e exclusão;
- checkout e operações de pagamento futuras;
- webhooks.

A chave é escopada por usuário, endpoint e período de retenção. Mesmo comando retorna o mesmo efeito; reutilização com payload diferente retorna conflito.

## Erro

```json
{
  "type": "/problems/goal-not-found",
  "title": "Meta não encontrada",
  "status": 404,
  "code": "GOAL_NOT_FOUND",
  "traceId": "..."
}
```

Mensagens não revelam existência de recurso de outro espaço. Detalhes técnicos ficam somente em logs seguros.

## Paginação e filtros

- cursor para coleções que crescem continuamente, como auditoria e contribuições;
- limite máximo configurado;
- ordenação estável com desempate por ID;
- filtros em query documentados e allowlisted.

## Compatibilidade

Mudanças aditivas compatíveis permanecem em `v1`. Remoção, renomeação ou mudança semântica exige depreciação e, se necessário, nova versão. Contratos gerados são verificados no CI para evitar drift.
