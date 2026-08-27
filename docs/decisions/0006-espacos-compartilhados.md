# ADR 0006 — Espaços compartilhados no MVP

- Estado: Aceito
- Data: 27/08/2026

## Contexto

O usuário determinou que metas compartilhadas por casal façam parte do MVP. O produto precisa colaborar sem expor automaticamente dados financeiros pessoais.

## Decisão

Introduzir `PlanningSpace` pessoal ou compartilhado como fronteira de autorização. Um espaço compartilhado aceita múltiplos proprietários e metas, cenários, perfil agregado e contribuições do espaço. Dados pessoais permanecem separados e só são compartilhados por ação explícita.

## Alternativas

- compartilhar uma senha/conta;
- criar apenas metas pessoais com link de leitura;
- compartilhar todo o perfil financeiro automaticamente.

## Consequências

- Atende colaboração real e evita conta conjunta artificial.
- Aumenta superfície de autorização, privacidade, conflito e exclusão.
- Exige testes por papel, histórico de alterações e regras sobre saída de membro.

## Questões abertas

- confirmação conjunta para exclusão definitiva;
- tratamento de contribuições atribuídas após saída;
- limite de membros no MVP;
- notificações obrigatórias de mudança sensível.

## Revisão

Validar com casais e revisão jurídica antes do beta.
