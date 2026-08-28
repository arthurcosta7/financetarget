# Fase 5 — Cenários e dashboard

## Objetivo e escopo

Permitir que a pessoa compare mudanças explícitas de prazo, inflação, retorno hipotético e momento do aporte sem alterar o plano base. A fase também amplia a criação para cinco tipos progressivos de meta e reúne as trajetórias ativas em um dashboard responsivo.

Incluído:

- Scenario Engine puro e versionado;
- até três cenários imutáveis por meta;
- deltas contra o snapshot base;
- histórico de cenários;
- dashboard de múltiplas metas;
- gráfico de prazo acompanhado por tabela acessível;
- exportação LGPD dos cenários criados pelo titular.

Excluído: recomendação de “melhor” cenário, edição ou exclusão de cenários, colaboração operacional, integrações, pagamentos, notificações reais e deploy.

## Critérios de aceite

- mesma entrada produz a mesma comparação;
- cenário não altera o snapshot nem o resultado do plano base;
- dinheiro permanece decimal exato no backend e string na API;
- leitura e criação validam associação e papel no espaço;
- cada cenário possui snapshot, hash, versões e timestamp;
- comparação possui equivalente textual/tabular ao gráfico;
- dashboard não executa fórmulas financeiras;
- reflow funciona em 320 px e nos dois temas.

## Resultado

O `ScenarioEngine` recebe uma base explícita e de um a três conjuntos alternativos, delega cada projeção ao `GoalEngine` e retorna diferenças de aporte, alvo nominal e prazo. Ele não classifica nem recomenda cenários.

A migration `V4` cria cenários, vincula snapshots e libera `HOME_DOWN_PAYMENT`, `EMERGENCY_RESERVE`, `VEHICLE`, `TRAVEL` e `CUSTOM`. O endpoint aninhado preserva a fronteira do espaço e o histórico é append-only.

O dashboard apresenta cada meta usando projeção e percentual já calculados pelo backend. A comparação usa uma linha de prazo para inspeção rápida e uma tabela semântica como fonte completa e acessível.

## Defeito encontrado no QA

O primeiro QA real detectou que a consulta do plano base selecionava o snapshot mais recente, incluindo um snapshot de cenário. O filtro passou a exigir `scenario_id is null`, a exportação foi alinhada e um teste de regressão garante que salvar um cenário não muda o aporte base.

## Gate

Implementação e validação concluídas em 28/08/2026. A fase aguarda aprovação explícita antes de qualquer trabalho da Fase 6.
