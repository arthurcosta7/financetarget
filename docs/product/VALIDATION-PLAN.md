# Plano de validação

## Objetivo

Reduzir as incertezas de maior risco antes de desenvolver integrações, pagamentos ou infraestrutura avançada.

## Hipóteses prioritárias

1. Existe dor recorrente ao transformar metas em um plano mensal.
2. Imóvel é um bom ponto de entrada, mas o valor se estende a outras metas.
3. Entrada manual é aceitável para o primeiro valor percebido.
4. Cenários aumentam compreensão e ação.
5. O usuário entende projeção sem interpretá-la como garantia.
6. Existe motivo para retorno mensal.
7. Há disposição a pagar por acompanhamento, não apenas por uma simulação isolada.
8. Usuários confiam em fornecer dados mínimos quando a finalidade é clara.

## Etapa 1 — Entrevistas de problema

### Amostra inicial proposta

Realizar de 12 a 20 entrevistas, com predominância da persona primária e presença das outras três. A amostra não pretende ter validade estatística; busca padrões, linguagem e objeções.

### Roteiro

1. Conte sobre uma meta financeira importante que você considera atualmente.
2. O que aconteceu na última vez que tentou planejar essa meta?
3. Que ferramentas ou pessoas você procurou?
4. Mostre, se se sentir confortável, como organizou o cálculo sem compartilhar dados sensíveis.
5. Qual foi a parte mais difícil?
6. O que fez você continuar ou abandonar o plano?
7. Que mudança recente faria você recalcular tudo?
8. Como sabe se está progredindo?
9. Que informação não forneceria a um produto desse tipo? Por quê?
10. Em que momento uma projeção pareceria confiável ou enganosa?
11. Você já pagou por planilha, aplicativo ou orientação financeira? O que motivou a decisão?
12. O que precisaria acontecer para voltar a usar a ferramenta no mês seguinte?

Evitar apresentar a solução antes de compreender o comportamento passado.

## Etapa 2 — Landing page

Estrutura:

1. mensagem principal;
2. problema reconhecível;
3. exemplo de uma meta;
4. explicação de cenários e premissas;
5. demonstração não funcional ou protótipo;
6. transparência sobre privacidade;
7. lista de espera.

Coletar somente o necessário:

- e-mail;
- principal tipo de meta;
- horizonte aproximado;
- método usado hoje;
- interesse em entrevista opcional.

Não solicitar renda exata na lista de espera sem necessidade demonstrada.

### Mensagens a comparar

- “Descubra o caminho para realizar suas metas.”
- “Seus planos têm um preço. Veja como chegar lá.”
- “Planeje hoje a vida que você quer construir.”

A avaliação deve considerar compreensão, qualidade do cadastro e resposta em entrevista, não apenas taxa de clique.

## Etapa 3 — Protótipo de compreensão

Testar com dados fictícios:

- criação de uma meta;
- contribuição mensal necessária;
- cenário de prazo maior;
- cenário de aporte maior;
- premissas;
- aviso de incerteza;
- próxima ação.

Observar se a pessoa consegue explicar com as próprias palavras:

- o que o número significa;
- o que não significa;
- qual decisão está sob seu controle;
- qual cenário prefere e por quê.

## Etapa 4 — Teste de preço

Testar conceitos de plano, ainda sem cobrança:

- simulação gratuita limitada;
- assinatura com acompanhamento e múltiplos cenários;
- plano familiar futuro;
- orientação humana como possível serviço separado, não prometido.

Usar perguntas de preço e intenção com cautela. Interesse declarado não equivale a compra. A validação final exige comportamento real em beta e, após autorização, checkout funcional.

## Instrumentação candidata

- `landing_viewed`;
- `waitlist_started`;
- `waitlist_completed`;
- `goal_type_selected`;
- `simulation_started`;
- `simulation_completed`;
- `assumptions_viewed`;
- `scenario_created`;
- `scenario_compared`;
- `goal_saved`;
- `progress_updated`;
- `data_export_requested`;
- `account_deletion_requested`.

Os eventos não devem conter renda, patrimônio, objetivo em texto livre ou outros dados financeiros identificáveis.

## Critérios de decisão

### Prosseguir

- padrões recorrentes confirmam o problema;
- usuários concluem a simulação com dados manuais;
- a maioria dos testes compreende projeção e premissas;
- existe uma ação recorrente plausível;
- intenção de pagamento é apoiada por comportamento, não apenas elogio.

### Ajustar

- há dor, mas o tipo de meta ou persona escolhida não é o melhor ponto de entrada;
- usuários querem uma resposta diferente da planejada;
- onboarding exige dados demais;
- cenários confundem mais do que ajudam.

### Interromper ou reformular

- o problema é raro ou resolvido satisfatoriamente por alternativas gratuitas;
- não há comportamento de retorno;
- o valor depende obrigatoriamente de integrações caras antes da validação;
- riscos de compreensão ou confiança superam o benefício percebido.
