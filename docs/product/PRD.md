# PRD — Planejamento financeiro por metas

## 1. Estado do documento

- Versão: 0.2
- Data: 27/08/2026
- Estado: Fase 0 aprovada; atualizado com decisões propostas da Fase 1
- Nome do produto: “Planejamento” é um nome interno provisório

## 2. Visão

Permitir que pessoas transformem objetivos de vida em planos financeiros compreensíveis, acompanhem o progresso e entendam como escolhas alteram prazo e esforço mensal.

## 3. Proposta de valor

> Descubra o caminho mensal para realizar sua meta e compare como cada escolha muda o prazo.

## 4. Público inicial

Persona primária provisória: profissional ou família com renda relativamente previsível, planejando uma entrada de imóvel em dois a cinco anos e hoje dependente de planilhas, calculadoras ou estimativas fragmentadas.

A definição é uma hipótese, não um recorte comercial definitivo. Consulte `PERSONAS.md`.

## 5. Princípios de produto

1. A meta vem antes da conta ou do produto financeiro.
2. Toda projeção mostra premissas e limitações.
3. O sistema apresenta escolhas, não julgamentos.
4. Valor inicial não depende de integração bancária.
5. O usuário controla seus dados.
6. Segurança e privacidade fazem parte da experiência.
7. Complexidade só entra quando resolve um problema validado.
8. Projeção nunca é garantia, promessa de retorno ou proposta de crédito.

## 6. Jornada principal do MVP

1. Criar conta e confirmar e-mail.
2. Escolher um espaço pessoal ou criar/entrar em um espaço compartilhado.
3. Concluir onboarding com dados financeiros mínimos e conscientemente compartilhados.
4. Informar renda, despesa agregada, saldo disponível e capacidade mensal estimada.
5. Cadastrar uma meta com valor e prazo.
6. Receber uma projeção com contribuição mensal necessária.
7. Ver fórmula em linguagem acessível, premissas e limitações.
8. Alterar prazo, aporte, saldo inicial, inflação ou retorno hipotético.
9. Comparar cenário-base e alternativas.
10. Salvar a meta e registrar progresso manual.
11. Exportar ou solicitar exclusão dos próprios dados.

## 7. Requisitos funcionais do MVP

### Identidade e privacidade

- RF-001: cadastrar, verificar, autenticar e encerrar sessão.
- RF-002: recuperar senha sem revelar a existência da conta.
- RF-003: visualizar e editar o próprio perfil.
- RF-004: registrar aceites e consentimentos aplicáveis de forma versionada.
- RF-005: exportar dados próprios.
- RF-006: solicitar exclusão da conta e acompanhar o estado do pedido.

### Espaços compartilhados

- RF-007: criar espaço pessoal automaticamente após verificação da conta.
- RF-008: criar espaço compartilhado e convidar uma pessoa por e-mail.
- RF-009: aceitar ou rejeitar convite após visualizar o escopo de dados compartilhados.
- RF-014: permitir múltiplos proprietários e papéis `OWNER`, `EDITOR` e `VIEWER`.
- RF-015: registrar autoria das mudanças relevantes.
- RF-016: impedir que exportação ou exclusão de um membro exponha ou apague indevidamente dados do parceiro.

### Perfil financeiro

- RF-010: registrar renda mensal ou faixa de renda.
- RF-011: registrar despesas essenciais agregadas.
- RF-012: registrar saldo inicial destinado a metas.
- RF-013: estimar capacidade mensal e permitir ajuste consciente.

### Metas

- RF-020: criar, consultar, editar, arquivar e excluir meta própria.
- RF-021: suportar reserva, imóvel, automóvel, viagem, aposentadoria, patrimônio e meta personalizada, com entrada progressiva por fase.
- RF-022: manter valor-alvo, data-alvo, saldo inicial, moeda e estado.
- RF-023: registrar contribuições e progresso manual.

### Goal Engine

- RF-030: calcular contribuição periódica necessária.
- RF-031: calcular data estimada dado um aporte.
- RF-032: calcular valor projetado em uma data.
- RF-033: explicitar periodicidade, inflação, retorno, taxas e arredondamento.
- RF-034: versionar fórmula, entradas e resultado.
- RF-035: retornar alertas para dados insuficientes, datas inválidas e planos incompatíveis com premissas.

### Cenários

- RF-040: criar cenários imutáveis ou versionados.
- RF-041: comparar ao menos três cenários.
- RF-042: explicar diferenças de prazo, aporte e valor acumulado.
- RF-043: distinguir cenário de recomendação e de garantia.

### Acompanhamento

- RF-050: apresentar dashboard de metas e progresso.
- RF-051: mostrar próximo marco e desvio do plano.
- RF-052: permitir revisão das premissas sem apagar histórico.

## 8. Requisitos não funcionais iniciais

- RNF-001: isolamento de dados por usuário e testes contra IDOR.
- RNF-002: precisão decimal explícita para dinheiro.
- RNF-003: cálculos determinísticos, reproduzíveis e testáveis fora do framework.
- RNF-004: WCAG 2.2 AA como objetivo mínimo.
- RNF-005: interface responsiva de celular compacto a desktop largo.
- RNF-006: API versionada e documentada por OpenAPI.
- RNF-007: logs estruturados sem PII financeira indevida.
- RNF-008: migrations versionadas e testadas.
- RNF-009: configuração validada no startup e segredos fora do repositório.
- RNF-010: integrações isoladas por hubs e adaptadores.
- RNF-011: observabilidade com métricas, traces, alertas e correlation ID.
- RNF-012: testes automatizados orientados ao risco e pipeline reproduzível.

Metas quantitativas iniciais de disponibilidade, latência, RPO e RTO estão propostas em `docs/architecture/OVERVIEW.md` e deverão ser validadas por testes antes do beta.

## 9. Escopo do MVP

Inclui:

- entrada manual;
- uma fatia vertical completa de meta;
- Goal Engine;
- cenários;
- acompanhamento manual;
- espaço compartilhado com convite, papéis e metas de casal;
- autenticação e direitos básicos do titular;
- mocks para pagamentos e notificações quando essas fases forem aprovadas.

## 10. Não objetivos do MVP

- movimentar dinheiro ou executar investimentos;
- recomendar ativos;
- conceder crédito;
- prometer rentabilidade ou realização;
- calcular score de crédito;
- conectar a bancos reais;
- armazenar credenciais bancárias;
- vender dados;
- oferecer consultoria financeira humana;
- depender de microserviços;
- implementar todas as categorias de meta simultaneamente.

## 11. Modelo de negócio — hipótese

Modelo freemium ou assinatura:

- experiência gratuita suficiente para comprovar valor;
- plano pago potencialmente libera múltiplas metas, cenários avançados, acompanhamento e integrações futuras;
- preço e limites permanecem em aberto até validação.

Nenhum benefício comercial ou preço está aprovado nesta fase.

## 12. Métricas

### Aquisição

- conversão qualificada da landing page;
- distribuição de metas declaradas;
- origem e custo por cadastro quando houver mídia.

### Ativação

- simulação concluída;
- meta salva;
- cenário alternativo compreendido;
- tempo até o primeiro plano útil;
- abandono por etapa e por campo.

### Retenção e valor

- retorno em 7, 30 e 90 dias;
- atualização de progresso;
- revisão de cenário;
- metas ativas por usuário;
- resposta qualitativa sobre confiança e ação.

### Negócio

- intenção e conversão pagante;
- receita recorrente;
- cancelamento;
- custo de serviço;
- margem por plano.

Metas numéricas serão definidas após estabelecer baseline. Não usar métricas de vaidade como critério isolado.

## 13. Riscos principais

| Risco | Impacto | Mitigação inicial |
|---|---|---|
| Usuário interpreta projeção como garantia | Alto | Linguagem, premissas, testes de compreensão e revisão jurídica |
| Vazamento de dados financeiros | Alto | Minimização, isolamento, criptografia, logs seguros e threat model |
| Cálculo incorreto | Alto | Núcleo puro, precisão decimal, testes de referência e versionamento |
| Baixa recorrência | Alto | Validar acompanhamento antes de integrações caras |
| Onboarding exige dados demais | Alto | Entrada progressiva e testes de abandono |
| Acoplamento a provedor | Médio/alto | Hubs, modelos canônicos e testes de contrato |
| Escopo regulatório mal interpretado | Alto | Não movimentar dinheiro, linguagem delimitada e revisão qualificada |
| Complexidade excessiva | Médio | Monólito modular e gates por fase |
| Disposição a pagar insuficiente | Alto | Teste de preço antes de checkout real |
| Colaboração expõe ou apaga dados do parceiro | Alto | PlanningSpace, autorização por papel, exportação escopada e workflow de exclusão |

## 14. Dependências futuras

- revisão jurídica e LGPD;
- provedor de pagamentos;
- provedor de notificações;
- parceiro ou agregador autorizado de Open Finance;
- fontes de viagens, fidelidade e financiamento;
- infraestrutura de produção e observabilidade.

Essas dependências não bloqueiam a validação do problema nem o primeiro fluxo manual.

## 15. Critério de lançamento beta

O beta fechado exigirá, no mínimo:

- problema e persona validados qualitativamente;
- cálculo verificado independentemente;
- fluxos críticos testados;
- isolamento de dados demonstrado;
- termos, privacidade e mensagens financeiras revisados;
- exportação e exclusão testadas;
- observabilidade, backup, restauração e rollback;
- suporte e resposta a incidentes;
- critérios explícitos de continuidade ou interrupção;
- convite, papéis e saída de espaço compartilhado testados contra acesso indevido.
