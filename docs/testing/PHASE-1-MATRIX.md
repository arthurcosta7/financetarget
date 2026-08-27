# Matriz de verificação derivada da Fase 1

Esta matriz orientará implementação e testes das fases seguintes.

| Risco/fluxo | Unidade | Integração | Contrato | E2E | Segurança/acessibilidade |
|---|---:|---:|---:|---:|---:|
| Money, taxa e arredondamento | Sim | Não | Não | Não | Não |
| Goal Engine | Sim/Propriedade | Referência independente | Não | Fluxo principal | Compreensão |
| Scenario Engine | Sim | Sim | Não | Comparação | Não depender de cor |
| Snapshot reproduzível | Sim | PostgreSQL | API | Histórico | Integridade |
| Autenticação e refresh | Sim | Banco/cookies | API | Login | Reuso, CSRF, enumeração |
| Espaço compartilhado | Sim | PostgreSQL | API | Convite e edição | IDOR e papéis |
| Conflito de edição | Sim | Concorrência | 409/ETag | Duas sessões | Sem overwrite silencioso |
| Contribuição idempotente | Sim | PostgreSQL | API | Registro | Replay |
| Exportação/exclusão | Sim | Job/banco | API | Solicitação | Dados do parceiro excluídos |
| Hub mock | Sim | Adaptador | Contrato canônico | Falha parcial | Timeout/redaction |
| Tema claro/escuro | Componente | Não | Não | Ambos | Contraste/foco/reflow |
| Linha de trajetória | Componente | Não | Não | Cenários | Resumo textual/motion reduzido |

## Gates futuros

- Toda fórmula crítica possui caso conhecido e verificação independente.
- Todo endpoint de espaço tem teste com usuário de outro espaço.
- Toda ação de papel tem caso owner/editor/viewer.
- Toda mutation concorrente testa versão obsoleta.
- Todo gráfico possui alternativa textual.
- Todo fluxo crítico passa em teclado, 320 px, 200% de zoom e tema escuro.
- Analytics nunca recebe valores financeiros ou texto livre de meta.
