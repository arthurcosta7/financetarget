# ADR 0014 — Resend para mensagens transacionais de identidade

- Estado: Aceito
- Data: 28/08/2026

## Contexto

O cadastro e a recuperação já produzem tokens opacos, persistem somente seus hashes e entregam mensagens por `IdentityMessagePort`. A entrega da Fase 3 era exclusivamente efêmera. O usuário autorizou especificamente o uso do Resend para verificação de e-mail, utilizando uma credencial local preexistente sob seu controle.

## Decisão

Adicionar um adaptador Resend atrás de `IdentityMessagePort`, sem SDK no domínio e sem alterar o contrato da API. O adaptador envia verificação e recuperação pela API HTTPS do provedor, com endpoint, remetente, URL pública e timeouts configuráveis.

A integração permanece desligada por padrão. Quando habilitada, a aplicação valida a chave, o remetente, as URLs e os timeouts no startup. O perfil de teste continua usando a caixa em memória. No perfil `dev`, habilitar Resend remove a caixa e o endpoint de inspeção para que tokens não permaneçam expostos por dois canais.

Contas pendentes podem solicitar reenvio por endpoint público com resposta neutra e rate limit. Cada solicitação elegível substitui o token anterior antes da entrega, evitando múltiplos links válidos.

Segredos ficam somente no `.env` ignorado ou em cofre do ambiente. Destinatário, token, link e corpo não são registrados em logs, métricas ou auditoria.

## Alternativas

- retornar o token ao navegador;
- manter somente a caixa de desenvolvimento;
- acoplar o serviço de identidade ao SDK do Resend;
- criar agora uma plataforma genérica para todos os canais;
- persistir token em texto claro numa outbox antes de existir proteção apropriada.

## Consequências

- contas locais podem receber links reais de verificação e recuperação;
- trocar o provedor continua localizado no adaptador;
- o provedor passa a tratar endereço de e-mail, conteúdo transacional e metadados técnicos;
- a entrega atual é síncrona e limitada por timeout, sem retry automático;
- indisponibilidade do provedor retorna erro temporário e reverte a criação do cadastro na transação atual.

## Riscos e mitigação

- vazamento de chave: arquivo ignorado, ausência de logs e rotação no painel do provedor;
- header injection no remetente: validação de quebra de linha e configuração fail-fast;
- phishing ou link incorreto: origem pública configurada e rota canônica do frontend;
- enumeração durante falha do provedor: resposta não inclui destinatário ou estado da conta, além do rate limit existente;
- transferência e retenção pelo operador: revisão contratual, suboperadores, região e mecanismo internacional antes de usuários reais;
- perda de entrega: monitorar falhas e definir outbox/retry antes do beta caso a evidência operacional exija.

## Condições de revisão

Revisar antes do beta, ao trocar provedor, adicionar marketing, habilitar múltiplas instâncias, exigir retry/outbox, usar domínio definitivo ou processar dados de usuários reais.

## Aprovação

Aceito pela autorização específica do usuário em 28/08/2026. Não representa aprovação geral da Fase 8 nem das demais integrações reais.
