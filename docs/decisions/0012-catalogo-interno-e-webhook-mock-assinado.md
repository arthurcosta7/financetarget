# ADR 0012 — Catálogo interno e webhook mock assinado

- Estado: Proposto
- Data: 28/08/2026

## Contexto

A Fase 6 precisa provar assinaturas e integrações sem escolher fornecedor, inventar preços ou permitir que um processador de pagamentos controle diretamente o acesso ao produto.

## Decisão

Manter catálogo, planos e entitlements como fonte interna. O `PaymentsHub` cria apenas uma sessão canônica e o evento externo informa fatos de cobrança. O módulo de assinaturas decide o estado e resolve benefícios pelo plano interno.

O adaptador da fase é exclusivamente mock. Seu webhook verifica HMAC sobre o corpo bruto antes do parsing, exige timestamp dentro de tolerância configurável e persiste somente hash e metadados mínimos para idempotência. A sessão simulada nunca ativa assinatura por si só.

## Alternativas

- colocar benefícios e limites no payload do provedor;
- hardcodar planos e preços no backend ou frontend;
- ativar a assinatura imediatamente ao criar checkout;
- adiar idempotência e verificação de assinatura até a integração real;
- introduzir um SDK de pagamento antecipadamente.

## Consequências

- troca de provedor não altera regras de acesso;
- catálogo precisa de operação própria antes de produção;
- testes exercitam replay e conflito sem cobrança;
- o mock não serve como adaptador de produção;
- enforcement de entitlement pode evoluir sem contaminar módulos de meta com DTO externo.

## Riscos e mitigação

- segredo fraco no ambiente local: comprimento mínimo e configuração explícita;
- evento repetido com conteúdo diferente: hash e conflito;
- confusão entre simulação e cobrança: copy explícita e ausência de preço;
- flag usada como autorização: segurança de endpoint permanece independente da flag.

## Condições de revisão

Revisar ao validar modelo comercial, definir downgrade, contratar provedor, criar ambiente de staging ou aplicar entitlements sobre limites existentes.

## Aprovação

Aguardando aprovação explícita da Fase 6.
