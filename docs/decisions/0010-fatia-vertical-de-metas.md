# ADR 0010 — Fatia vertical de metas e snapshot inicial

- Estado: Aceito
- Data: 28/08/2026

## Contexto

A primeira implementação do Goal Engine precisa provar a jornada completa sem antecipar a complexidade de cenários, provedores ou categorias especializadas.

## Decisão

Implementar inicialmente a categoria `HOME_DOWN_PAYMENT`, com período mensal derivado de `YearMonth`, taxas efetivas convertidas com `BigDecimal` e aporte configurável no início ou fim do mês.

Persistir um snapshot imutável na criação da meta. Contribuições posteriores são eventos idempotentes separados: atualizam o progresso manual, mas não alteram nem recalculam o snapshot. O Goal Engine é puro e independente de Spring; o frontend apenas envia premissas e apresenta o resultado do backend.

## Alternativas

- implementar todas as categorias do MVP de uma vez;
- manter somente o resultado atual e sobrescrevê-lo a cada contribuição;
- calcular projeções também no frontend;
- introduzir o Scenario Engine junto da primeira meta.

## Consequências

- existe uma jornada pequena, auditável e reproduzível;
- versões antigas de cálculo permanecem explicáveis;
- histórico de contribuição não altera evidência matemática anterior;
- múltiplas categorias e cenários exigem evolução contratual posterior;
- ajustes de premissas criarão novos artefatos, não mutação silenciosa.

## Riscos e mitigação

- interpretação incorreta de taxa decimal: rótulos explícitos, tipos string e explicação do snapshot;
- divergência matemática: testes puros, hash canônico e versões de fórmula;
- excesso de confiança: avisos obrigatórios de limitações e ausência de garantias;
- crescimento de snapshots: índices e política de retenção serão revistos antes do beta.

## Condições de revisão

Reavaliar ao introduzir cenários, mudanças de premissas, eventos extraordinários, novas moedas ou validação matemática independente.

## Aprovação

Aceito com a aprovação da Fase 4 e autorização da Fase 5 em 28/08/2026.
