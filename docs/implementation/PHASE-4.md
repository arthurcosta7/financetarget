# Fase 4 — Fatia vertical de metas

## Objetivo e escopo

Entregar a primeira jornada autenticada de meta pessoal, da criação ao acompanhamento manual, com cálculo financeiro reproduzível no backend. A fase inclui uma meta de entrada de imóvel, Goal Engine puro, snapshot imutável, progresso, contribuição manual e explicação das premissas.

Scenario Engine, comparação de cenários, colaboração operacional, integrações, pagamentos e deploy permanecem fora desta fase.

## Critérios de aceite

- jornada autenticada ponta a ponta entre onboarding, criação, detalhe e contribuição;
- dinheiro e taxas transportados como strings decimais, sem `float` ou `double`;
- cálculo independente de framework, com versões de motor e fórmula;
- snapshot imutável com entradas, resultado, avisos e hash canônico;
- autorização por espaço e resposta 404 para recurso de outro espaço;
- contribuição idempotente sem recalcular ou alterar o snapshot original;
- nenhuma fórmula financeira no frontend;
- OpenAPI, tipos gerados, LGPD, documentação e testes atualizados.

## Resultado

O `GoalEngine` converte taxas efetivas anuais em mensais com `BigDecimal`, projeta o valor-alvo e calcula o aporte mensal no início ou no fim do período. A convenção usa `DECIMAL128` internamente e `HALF_EVEN` com duas casas na apresentação monetária.

A criação persiste a meta e um snapshot imutável na mesma transação. Contribuições são eventos separados e idempotentes. O progresso exibido é calculado pelo backend a partir do valor inicial e das contribuições registradas.

A interface mantém a direção monocromática e invertível: criação editorial em duas colunas, linha de trajetória, premissas legíveis, limitações explícitas e reflow para 320 px. A revisão móvel identificou e corrigiu truncamento dos valores do snapshot.

## Segurança e privacidade

- leitura e escrita exigem associação ao `PlanningSpace`;
- escrita exige papel `OWNER` ou `EDITOR`;
- CSRF permanece obrigatório para criação e contribuição;
- a chave de idempotência é única por meta e usuário;
- exportação LGPD inclui metas e contribuições do titular;
- testes e QA usam exclusivamente dados sintéticos.

## Gate

Fase aprovada pelo usuário em 28/08/2026 ao autorizar a Fase 5. O Scenario Engine continua deliberadamente separado para a fase seguinte.
