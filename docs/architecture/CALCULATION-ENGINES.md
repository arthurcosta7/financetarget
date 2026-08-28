# Goal Engine e Scenario Engine

## Fronteira

Os motores são bibliotecas internas puras de domínio. Não acessam banco, relógio global, HTTP, sessão, configuração externa ou provedor. Recebem entradas completas e retornam resultado tipado com avisos.

## Tipos essenciais

- `Money`: valor decimal + moeda;
- `AnnualRate`: taxa efetiva anual decimal;
- `MonthlyRate`: taxa efetiva mensal decimal;
- `TargetValueBasis`: `CURRENT_VALUE` ou `FIXED_NOMINAL`;
- `ContributionTiming`: final ou início do período;
- `ProjectionPeriod`: data-base, data-alvo e quantidade de períodos;
- `AssumptionSet`: inflação, retorno, custos e convenções;
- `EngineVersion` e `FormulaVersion`.

## Convenções implementadas no Goal Engine

- Período padrão do MVP: mensal.
- Aporte padrão: final do mês, explicitado no resultado.
- Taxas: efetivas, não nominais, salvo indicação explícita.
- Conversão anual para mensal: `r_m = (1 + r_a)^(1/12) - 1`.
- Precisão interna: equivalente a `DECIMAL128` ou superior.
- Arredondamento de apresentação monetária: casas mínimas da moeda, `HALF_EVEN`.
- O valor usado em decisões nunca é derivado de texto já formatado.
- Moeda base inicial: configurada pelo espaço; nenhuma moeda é implícita no motor.

## Valor-alvo

Para `FIXED_NOMINAL`, o valor informado já é o valor futuro.

Para `CURRENT_VALUE`, o motor atualiza o poder de compra até a data-alvo:

```text
targetFuture = targetToday × (1 + inflationAnnual)^(months / 12)
```

O resultado sempre informa qual base foi utilizada.

## Acumulação

Para `n` meses, taxa mensal `r`, saldo inicial `P` e contribuição mensal `C` no fim do período:

```text
futureValue = P × (1 + r)^n + C × (((1 + r)^n - 1) / r)
```

Quando `r = 0`:

```text
futureValue = P + C × n
```

Para aporte no início do período, o componente de contribuições é multiplicado por `(1 + r)`.

## Contribuição necessária

```text
requiredContribution =
  (targetFuture - P × (1 + r)^n) / annuityFactor
```

Se o saldo projetado inicial já alcançar a meta, a contribuição requerida é zero e um aviso explica o caso. Resultado negativo nunca é exibido como “aporte negativo”.

## Data estimada

A data estimada será obtida por simulação mensal discreta usando a mesma ordem de eventos do cálculo principal. Evita divergência de convenções causada por uma fórmula logarítmica separada e permite incluir aportes extraordinários.

Limites de iteração devem ser explícitos. Se a meta não convergir dentro do horizonte configurado, o motor retorna `UNREACHABLE_WITH_ASSUMPTIONS`, não uma data artificial.

## Eventos extraordinários

O modelo aceita uma série ordenada de fluxos pontuais futuros. No MVP eles são manuais. Cada fluxo possui data, valor, moeda e natureza. Fluxos negativos exigem validação de caso de uso e não podem tornar silenciosamente o saldo inválido.

## Custos, impostos e retorno

- Custos e impostos não serão presumidos.
- Quando não modelados, o resultado declara a exclusão.
- Retorno é uma hipótese editável, nunca promessa.
- Presets não contêm taxas compiladas no código; valores vêm de configuração versionada ou entrada confirmada.

## Saída do Goal Engine

```text
GoalProjection
  targetNominal
  requiredContribution
  projectedValueAtTarget
  estimatedCompletionDate?
  totalContributed
  projectedGrowth
  shortfallOrSurplus
  normalizedInputs
  warnings[]
  engineVersion
  formulaVersion
```

## Scenario Engine

Responsabilidades:

1. validar conjuntos de premissas;
2. solicitar uma projeção ao Goal Engine para cada conjunto;
3. calcular deltas entre cenários;
4. produzir uma comparação ordenada e explicável;
5. manter a identidade e versão de cada cenário.

Não escolhe “o melhor” cenário nem recomenda investimento. `BASE`, `CONSERVATIVE` e `OPTIMISTIC` são rótulos de apresentação; suas taxas precisam ser fornecidas e mostradas.

## Snapshot

Cada cálculo persistido inclui:

- identificadores de espaço, meta e cenário;
- data-base;
- entradas originais e normalizadas;
- resultado completo;
- avisos;
- convenções de periodicidade e arredondamento;
- versão do motor e fórmula;
- hash canônico das entradas;
- timestamp e origem do pedido.

Snapshots são imutáveis. Recalcular cria um novo snapshot.

## Códigos de aviso candidatos

- `TARGET_ALREADY_FUNDED`;
- `UNREACHABLE_WITH_ASSUMPTIONS`;
- `NEGATIVE_RETURN_ASSUMPTION`;
- `INFLATION_NOT_INCLUDED`;
- `FEES_NOT_INCLUDED`;
- `TAXES_NOT_INCLUDED`;
- `TARGET_DATE_TOO_CLOSE`;
- `CONTRIBUTION_EXCEEDS_DECLARED_CAPACITY`;
- `PROJECTION_NOT_GUARANTEE`.

## Matriz mínima de verificação

| Caso | Resultado esperado |
|---|---|
| Taxa zero | Soma linear exata |
| Saldo inicial igual à meta | Aporte requerido zero |
| Meta em valor atual | Inflação aplicada pelo prazo |
| Taxa anual negativa válida | Projeção calculada e aviso emitido |
| Data-alvo inválida | Erro de domínio, sem snapshot |
| Moedas divergentes | Erro de domínio |
| Prazo muito longo | Respeita precisão e limite configurado |
| Aporte extraordinário | Aplicado no período correto |
| Início versus fim do mês | Diferença conforme convenção |
| Repetição da mesma entrada | Mesmo resultado e mesmo hash canônico |

Na Fase 4, taxa zero, valor atual, meta financiada, retorno negativo, capacidade declarada, prazo inválido e momento do aporte foram cobertos por testes puros. Uma validação matemática externa que não reutilize o algoritmo de produção permanece obrigatória antes do beta.
