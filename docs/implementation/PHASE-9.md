# Fase 9 — Produção

## Objetivo

Preparar um caminho de produção rastreável, reversível e fechado por padrão, provando localmente o artefato que será promovido sem escolher infraestrutura em nome do usuário, publicar o sistema ou processar dados reais.

## Escopo autorizado

- imagens OCI separadas para web e API, construídas de bases fixadas por digest e executadas sem root;
- identificação do release pelo SHA Git completo, presente no artefato e no status público sem dados sensíveis;
- perfil `production` com shutdown gracioso, logs estruturados, métricas e configuração estrita;
- guard de startup para release rastreável, documentos aprovados, cookies/origens seguros, Resend e integrações permitidas;
- contrato de gateway same-origin, TLS, redes e secrets independente de provedor;
- smoke read-only de readiness, schema, release, cookie CSRF, headers e rota opcional do gateway;
- topologia efêmera de validação com PostgreSQL sintético, filesystem read-only e capabilities removidas;
- CI para construir e executar as imagens com o perfil de produção;
- checklist manual, registro de release, rollout gradual, rollback e dependências operacionais.

## Fora do escopo

- escolher ou contratar nuvem, DNS, registry, cofre, observabilidade, e-mail ou armazenamento;
- criar domínio, certificado, banco, bucket, conta, segredo ou ambiente externo;
- publicar imagens, executar deploy ou abrir tráfego;
- habilitar beta, cadastrar participantes ou usar dados reais;
- aprovar termos, RIPD, retenções, responsáveis ou os motores financeiros;
- habilitar pagamentos, Open Finance ou qualquer hub futuro.

## Critérios de aceite técnico

| Critério | Evidência exigida |
|---|---|
| Artefatos são rastreáveis | labels OCI e `/api/v1/system/status` usam o mesmo SHA completo |
| Processo não roda como root | inspeção automática das duas imagens e usuário numérico/não privilegiado |
| Configuração falha fechada | testes rejeitam SHA inválido, documento provisório, Resend ausente, beta, mock, origem ou cookie inseguro |
| Imagem promovida é a mesma | URLs públicas não são embutidas no build; gateway same-origin fornece `/api` |
| Banco evolui antes do tráfego | readiness aguarda Flyway e smoke exige schema `7` |
| Release pode ser verificado | smoke compara release, schema, CSRF seguro e headers web |
| Rollback é deliberado | versão anterior, compatibilidade do schema, decisão e evidência têm campos obrigatórios |
| Nenhum efeito externo ocorre | validação usa banco efêmero, endereços `.test`, credenciais sintéticas e não envia mensagens |

## Gate humano e externo

A preparação técnica não conclui o lançamento. A Fase 9 permanece no gate até haver:

1. plataforma, registry, banco, cofre, DNS/TLS, collector/Alertmanager e armazenamento de backup aprovados;
2. responsáveis nominais e substitutos para release, incidente, privacidade, suporte e banco;
3. revisão jurídica/RIPD, retenções e operadores aprovados;
4. validação matemática independente registrada;
5. restauração exercitada no ambiente externo e alertas entregues a um canal operado;
6. SPF, DKIM, DMARC, bounce e complaint do domínio transacional verificados;
7. registro de release preenchido e autorização manual de rollout.

Sem esses itens, pare após a evidência técnica e não adapte o guard para contornar o gate.
