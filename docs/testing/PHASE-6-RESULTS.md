# Resultados de validação da Fase 6

## Automação

Executado em 28/08/2026:

- OpenAPI 0.5 e tipos TypeScript regenerados;
- lint e TypeScript estrito;
- 12 testes frontend em 6 arquivos;
- build de produção do Next.js incluindo `/app/plano`;
- 24 testes backend pelo Maven, sem falhas;
- migrations `V1` a `V5` em PostgreSQL 17.11 efêmero;
- empacotamento Maven e verificação de whitespace.

## Assinatura e integração

- catálogo e entitlements vêm do PostgreSQL, não do payload do provedor;
- checkout mock é idempotente e reutilização da chave com outro pedido é conflito;
- conflito concorrente da chave de checkout é absorvido pela unicidade e relido pelo serviço;
- assinatura inválida é rejeitada antes do parsing de negócio;
- timestamp fora da tolerância é rejeitado;
- mesmo evento e payload retornam sucesso sem duplicar assinatura ou notificação;
- mesmo identificador com outro payload retorna conflito;
- status canônico e entitlement são consultáveis somente com sessão autenticada;
- nenhuma biblioteca ou SDK de pagamento foi adicionado.

## Notificações e privacidade

- comunicação essencial permanece separada das três escolhas opcionais;
- preferências são persistidas por titular;
- entrega mock registra intenção sem endereço, corpo de mensagem ou envio;
- exportação própria inclui assinatura e preferências, sem referências de checkout ou webhooks.

## Frontend e QA visual

- tela exercitada com conta e catálogo exclusivamente sintéticos;
- desktop em 1440 × 900, tema escuro;
- mobile em 320 × 800, tema claro;
- largura do documento igual à largura útil nos dois viewports;
- títulos e navegação possuem nomes acessíveis;
- mensagens essenciais estão identificadas e desabilitadas para edição;
- nenhum erro ou aviso no console;
- nenhuma interface apresenta preço, cobrança concluída ou integração futura como ativa.

## Riscos residuais

- concorrência extrema de dois primeiros webhooks iguais ainda deverá receber teste de carga na Fase 7;
- catálogo e transições de downgrade precisam de operação administrativa antes de uma oferta real;
- segredo do mock é local; produção deve rejeitar o mock e usar segredo em cofre;
- notificações reais exigirão base legal, template versionado, unsubscribe aplicável e adapter próprio;
- endpoints e flags precisam de métricas e alertas na Fase 7.
