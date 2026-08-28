# ADR 0011 — Cenários como snapshots independentes

- Estado: Proposto
- Data: 28/08/2026

## Contexto

Comparações precisam ser reproduzíveis, não podem transformar hipóteses em recomendações e não devem substituir silenciosamente a projeção escolhida como plano atual.

## Decisão

Tratar cada cenário como artefato imutável vinculado a uma meta e a um snapshot próprio. Comparar de um a três cenários contra o snapshot base. O Scenario Engine delega cálculos ao Goal Engine e produz apenas deltas de aporte, alvo e prazo.

Consultas do plano base devem selecionar exclusivamente snapshots sem `scenario_id`. O frontend recebe resultados e deltas prontos; somente escala posições visuais a partir dos prazos retornados.

## Alternativas

- sobrescrever as premissas da meta ao simular;
- calcular cenários somente no navegador;
- persistir apenas taxas, recalculando sempre com a versão atual;
- permitir quantidade ilimitada desde o início;
- rotular automaticamente cenários como melhor, conservador ou otimista.

## Consequências

- base e alternativas permanecem auditáveis e isoladas;
- mudanças futuras de motor não reescrevem resultados históricos;
- o usuário precisa criar outro cenário para testar outra combinação;
- armazenamento cresce de forma previsível e limitada;
- nenhuma semântica de recomendação é introduzida.

## Riscos e mitigação

- cenário aparecer como base: filtro explícito e teste de regressão;
- confusão entre diferença negativa e perda: texto explica que o delta se refere ao aporte mensal;
- excesso de opções: limite de três e comparação tabular;
- taxas incompreensíveis: entradas e histórico exibem valores decimais declarados.

## Condições de revisão

Reavaliar ao permitir edição do plano base, arquivamento, cenários compartilhados, presets versionados ou mais de três alternativas.

## Aprovação

Aguardando aprovação explícita da Fase 5.
