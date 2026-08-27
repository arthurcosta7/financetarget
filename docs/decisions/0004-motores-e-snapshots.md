# ADR 0004 — Motores puros e snapshots imutáveis

- Estado: Proposto
- Data: 27/08/2026

## Contexto

Resultados financeiros precisam ser precisos, explicáveis e reproduzíveis mesmo quando fórmulas ou premissas evoluírem.

## Decisão

Implementar Goal Engine e Scenario Engine como núcleo puro, com decimal exato, convenções explícitas e nenhuma dependência de framework. Cada cálculo persistido gera snapshot imutável com entradas, saídas, avisos e versões.

## Alternativas

- calcular no frontend;
- recalcular sempre com a fórmula mais nova;
- salvar apenas o resultado final.

## Consequências

- Testes e auditoria mais fortes.
- Maior volume de histórico e necessidade de versionar schemas de snapshot.
- Mudanças de fórmula exigem estratégia explícita, não migração silenciosa.

## Revisão

Revisar convenções matemáticas antes da implementação e com especialista financeiro quando o produto incluir impostos, CET ou instrumentos regulados.
