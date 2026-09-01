# Premissas e lacunas de evidência

Nenhuma premissa abaixo deve ser apresentada como dado de mercado ou comportamento comprovado. A escala de confiança é: baixa, média ou alta. Nesta fase, quase toda afirmação sobre público tem confiança baixa porque ainda não houve pesquisa primária.

| ID | Premissa | Importância | Confiança | Como validar |
|---|---|---|---|---|
| A-001 | Pessoas com renda relativamente previsível têm dificuldade em converter metas de alto valor em um plano mensal confiável. | Alta | Baixa | Entrevistas e teste de protótipo |
| A-002 | O principal concorrente inicial é “continuar adiando” ou usar uma planilha incompleta, não outro SaaS. | Alta | Baixa | Entrevistas sobre comportamento atual |
| A-003 | Uma meta de imóvel produz urgência e disposição a fornecer dados suficientes para uma simulação. | Alta | Baixa | Landing page segmentada e entrevistas |
| A-004 | Transparência das premissas aumenta confiança mais do que uma interface que esconde os cálculos. | Alta | Baixa | Teste comparativo de protótipos |
| A-005 | O usuário retorna mensalmente para registrar progresso e rever cenários. | Alta | Baixa | Concierge MVP e coorte beta |
| A-006 | A persona primária aceita começar com entrada manual antes de Open Finance. | Alta | Baixa | Entrevista e teste de ativação |
| A-007 | Comparar cenários gera mais valor que apenas exibir um número final. | Alta | Baixa | Teste de usabilidade |
| A-008 | Usuários entendem “projeção” sem interpretar o resultado como garantia. | Alta | Baixa | Teste de compreensão |
| A-009 | Múltiplas metas são importantes, mas uma única meta é suficiente para o primeiro momento de valor. | Alta | Média | Protótipo e análise de ativação |
| A-010 | A faixa inicial de renda familiar mensal pode se concentrar aproximadamente entre R$ 7 mil e R$ 25 mil. | Média | Baixa | Recrutamento amplo e entrevistas |
| A-011 | O produto pode cobrar assinatura recorrente em vez de venda avulsa de simulação. | Alta | Baixa | Teste de preço e pré-venda sem cobrança |
| A-012 | A conexão por Open Finance aumenta retenção, mas não é necessária para validar o problema. | Alta | Baixa | Beta manual antes de integrar |
| A-013 | Imóvel, reserva, viagem e automóvel cobrem a maior parte das primeiras intenções. | Média | Baixa | Lista de espera com pergunta aberta |
| A-014 | Usuários aceitam compartilhar renda, despesas e patrimônio se a finalidade e a proteção forem claras. | Alta | Baixa | Entrevista de confiança e teste de abandono |
| A-015 | Um monólito modular é suficiente para o lançamento inicial. | Alta | Média | Revisão arquitetural e requisitos de escala |
| A-016 | Planejamento compartilhado aumenta o valor para casais sem tornar o fluxo pessoal confuso. | Alta | Baixa | Teste de fluxo com casais e pessoas solo |
| A-017 | A identidade monocromática transmite confiança e diferenciação sem prejudicar compreensão dos cenários. | Média | Baixa | Protótipo claro/escuro e teste de compreensão |
| A-018 | Um único domínio com gateway same-origin atende a primeira operação sem exigir URL pública embutida no frontend. | Alta | Média | Provar roteamento, cookies e observabilidade no ambiente externo escolhido |

## Prioridade de validação

As premissas A-001, A-003, A-005, A-006, A-008, A-011, A-014 e A-016 combinam alta importância com baixa confiança e devem ser testadas antes de investimentos relevantes em integrações ou infraestrutura.

## Decisões ainda abertas

- Nome e identidade da marca.
- Persona primária definitiva.
- Modelo e faixa de preço.
- Nível de detalhamento de despesas no onboarding.
- Política exata de projeções, inflação, taxas e impostos.
- Fornecedor futuro de autenticação, pagamentos, notificações e Open Finance.
- Necessidade e formato de revisão jurídica especializada.
- Regra de exclusão, saída e atribuição histórica em espaços compartilhados.
- Limite de membros e necessidade de confirmação conjunta para ações destrutivas.
- Plataforma, registry, banco, cofre, DNS/TLS, observabilidade e armazenamento de backup de produção.
