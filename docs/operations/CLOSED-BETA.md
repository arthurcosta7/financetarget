# Operação do beta fechado

## Estado deste runbook

Este documento prepara a operação, mas não autoriza deploy, recrutamento, convite, envio ou tratamento de dados reais. A flag `APP_BETA_ENABLED` permanece desligada por padrão e o guard atual rejeita sua ativação em staging e produção. Qualquer mudança desse bloqueio exige autorização específica, ADR revisado e checklist humano aprovado.

## Responsabilidades a nomear

Antes do primeiro participante, registrar nominalmente:

- responsável pelo produto e decisão de continuar, ajustar ou interromper;
- responsável técnico e substituto;
- encarregado ou ponto focal de privacidade;
- responsável pelo suporte e janela de atendimento;
- responsável por incidentes e comunicação;
- especialista independente que validou os motores e mensagens de projeção.

Nenhuma função pode ficar implícita em uma conta compartilhada.

## Checklist de entrada

- [ ] revisão jurídica de termos, aviso, colaboração, retenção e Resend concluída;
- [ ] RIPD e inventário de operadores aprovados;
- [ ] Goal Engine, Scenario Engine e mensagens validados de forma independente;
- [ ] responsáveis e escala de incidente registrados;
- [ ] ambiente externo isolado com TLS, cofre, alertas e backup exercitado;
- [ ] domínio transacional com SPF, DKIM, DMARC, bounce e complaint monitorados;
- [ ] política de recrutamento, consentimento de pesquisa e suporte aprovada;
- [ ] retenção de eventos e feedback definida e automatizável;
- [ ] smoke de cadastro, colaboração, exportação e interrupção da flag aprovado;
- [ ] autorização manual de entrada registrada com data e escopo.

## Operação diária proposta

1. Verificar disponibilidade, erros, latência e alertas técnicos sem abrir payloads.
2. Triar feedback por categoria e severidade, sem copiar comentários para canais amplos.
3. Conferir convites anormais, falhas de autorização e rejeições por rate limit.
4. Registrar hipóteses de aprendizagem somente em dados agregados.
5. Revisar qualquer relato de falsa garantia financeira com produto e especialista.
6. Encerrar o período com incidentes, decisões, responsáveis e prazos registrados.

Não consultar valores, títulos de meta ou perfil financeiro para medir uso. O armazenamento interno do beta aceita somente eventos e dimensões da allowlist contratual.

## Critérios de pausa imediata

Interromper entrada e coleta do beta se houver:

- suspeita de IDOR, exposição de dados, tomada de conta ou abuso de convite;
- cálculo materialmente incorreto ou snapshot não reproduzível;
- interpretação recorrente de projeção como promessa ou recomendação;
- indisponibilidade sem recuperação segura;
- coleta de propriedade não autorizada ou dado financeiro em telemetria;
- ausência do responsável necessário para responder ao impacto.

A contenção técnica preferida é desligar a flag em uma mudança controlada e seguir `INCIDENT-RESPONSE.md`. Não apagar registros nem executar `repair` de migration para ocultar o problema.

## Métricas permitidas

- contagem de eventos por nome, etapa, resultado e classe aproximada de dispositivo;
- conversão entre etapas usando identificadores internos minimizados;
- categorias e avaliações agregadas de feedback;
- volume e estado de convites, sem expor destinatários em relatórios;
- disponibilidade, latência e erros técnicos.

São proibidos em analytics: renda, patrimônio, valores, títulos, premissas numéricas, e-mail, texto de meta, payload, URL com parâmetros e conteúdo de suporte.

## Encerramento e decisão

Ao final do período autorizado, registrar evidências para uma das decisões: prosseguir com escopo explícito, ajustar e repetir, ou interromper. Exportar ou excluir dados somente conforme política jurídica aprovada. A aprovação técnica da Fase 8 não equivale a aprovação da Fase 9.
