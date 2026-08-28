# Estratégia de hubs de integração

## Regra central

O domínio depende de portas canônicas internas. SDK, DTO, nomenclatura, autenticação e erro de provedor permanecem no adaptador correspondente.

## Portas planejadas

| Hub | Responsabilidade futura | Modelo canônico principal |
|---|---|---|
| `BankingDataHub` | Consentimentos e dados Open Finance | conexão, conta, saldo, transação |
| `LoyaltyHub` | Programas, saldo e validade | programa, pontos, expiração |
| `TravelHub` | Estimativas de viagem | itinerário, preço, moeda, instante da consulta |
| `RealEstateFinancingHub` | Simulações de financiamento imobiliário | proposta indicativa, CET, prazo, sistema |
| `AutoFinancingHub` | Simulações automotivas | proposta indicativa, CET, prazo |
| `PaymentsHub` | Assinaturas e pagamentos | cliente, assinatura, cobrança, entitlement |
| `NotificationHub` | Entrega por canal | mensagem, destinatário, template, estado |

## Contrato obrigatório de um adaptador

- timeout explícito;
- classificação de erro em transitório, permanente, autenticação ou limite;
- retry apenas para operação segura e idempotente;
- circuit breaker no limite de infraestrutura;
- correlação e métricas;
- redaction de dados sensíveis;
- validação de resposta;
- sandbox e mock determinístico;
- testes de contrato;
- feature flag;
- documentação de escopos e credenciais.

## Fallback

- Nunca transformar indisponibilidade em sucesso fictício.
- Dados em cache devem mostrar fonte e momento da atualização.
- Cálculo manual continua disponível quando uma integração opcional falha.
- Falha de uma integração não altera snapshots já confirmados.

## Webhooks

- assinatura verificada antes do parsing de negócio;
- timestamp e tolerância contra replay;
- payload bruto protegido apenas pelo tempo necessário à verificação/auditoria;
- evento externo único por provedor;
- processamento idempotente;
- estado desconhecido leva à reconciliação, não a suposição.

Na Fase 6, essas propriedades são exercitadas somente pelo adaptador `MOCK`: HMAC sobre bytes brutos, tolerância temporal configurável, hash do payload e unicidade por provedor/evento. Nenhum payload bruto é retido.

## Open Finance

Não faz parte do MVP conectado. O desenho futuro exige parceiro autorizado, consentimento explícito, escopos mínimos, renovação, revogação, expiração e reconciliação. O produto não armazenará credenciais bancárias.

## Entitlements

Benefícios do plano pertencem ao domínio de assinatura. `PaymentsHub` informa fatos de cobrança, mas não define sozinho se uma funcionalidade está liberada. Isso permite troca de provedor e correção de divergências por reconciliação.

O catálogo interno e o mock foram implementados na Fase 6. Os demais hubs possuem somente portas canônicas e flags desligadas; não existe conexão externa.

## Evolução segura

Cada provedor real requer ADR próprio, análise jurídica, custos, sandbox, threat model atualizado, testes de contrato, liberação por flag e plano de desativação.
