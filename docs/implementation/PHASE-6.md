# Fase 6 — Assinaturas, notificações e hubs simulados

## Objetivo

Provar contratos, persistência, isolamento e falhas de integrações antes de escolher qualquer fornecedor ou realizar efeitos externos.

## Escopo executado

- catálogo interno de planos e entitlements, sem preço compilado;
- assinatura canônica por conta, independente do provedor;
- checkout mock idempotente que não cobra nem ativa plano;
- webhook mock com HMAC, janela antirreplay, hash do payload e processamento idempotente;
- preferências separadas para mensagens essenciais, lembretes, produto e marketing;
- intenção de notificação persistida como `SIMULATED`, `SUPPRESSED` ou `DISABLED`;
- feature flags tipadas e desligadas por padrão;
- portas canônicas mínimas para pagamentos, notificações, Open Finance, fidelidade, viagens e financiamentos;
- exportação LGPD ampliada para assinatura e preferências;
- tela autenticada de plano e comunicações, responsiva e sem preços fictícios.

Ficaram excluídos: cobrança, envio, SDK externo, credencial real, conexão bancária, sandbox de fornecedor, enforcement comercial sobre metas existentes, deploy e administração de catálogo.

## Critérios de aceite

| Critério | Evidência |
|---|---|
| O provedor não define benefícios | `plan_entitlement` é interno; webhook informa apenas plano e estado |
| Checkout repetido não duplica efeito | chave escopada por conta e hash do pedido |
| Evento falso ou antigo é rejeitado | HMAC validado sobre bytes brutos antes do JSON e janela configurável |
| Repetição do mesmo evento é idempotente | unicidade por provedor/evento e teste de regressão |
| Nenhum efeito externo acontece | únicos adaptadores concretos são mocks locais |
| Preferências opcionais são revogáveis | `PUT /notification-preferences`; essencial permanece separada |
| Flags não substituem autorização | endpoint informa flags, enquanto API continua fechada por sessão |
| Contratos externos não contaminam domínio | portas canônicas sem DTO ou SDK de fornecedor |
| Valores comerciais não estão hardcoded | catálogo sintético somente no seed `dev`; produção exige provisionamento |
| Interface é acessível e responsiva | landmarks, labels, foco, 320 px e ambos os temas validados |

## Sequência operacional

1. O produto consulta seu próprio catálogo e benefícios.
2. A conta cria uma sessão simulada idempotente.
3. A sessão não altera acesso.
4. Um evento mock assinado informa um fato de assinatura.
5. O módulo de assinaturas traduz o plano interno em entitlements.
6. Uma intenção essencial é registrada pelo `NotificationHub` mock.

## Limites deliberados

- Não existe preço ou moeda na fase porque o modelo comercial não foi validado.
- Entitlements são consultáveis, mas ainda não bloqueiam metas e cenários existentes; enforcement requer definição comercial e testes de downgrade.
- O webhook mock existe para exercitar segurança e idempotência. Um provedor real exige outro adaptador e ADR.
- O catálogo do ambiente `dev` é sintético; não é oferta comercial.

## Gate

Fase 6 aprovada explicitamente em 28/08/2026. O ADR 0012 foi aceito e a Fase 7 foi autorizada.
