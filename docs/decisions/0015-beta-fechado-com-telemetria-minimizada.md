# ADR 0015 — Beta fechado com telemetria minimizada e gate humano

- Estado: Proposto
- Data: 28/08/2026

## Contexto

A Fase 8 precisa gerar evidência de uso e compreensão sem permitir que analytics ou suporte se tornem uma cópia paralela de dados financeiros. A autorização da fase não substitui revisão jurídica, validação matemática ou decisão de deploy.

## Decisão

Registrar eventos de produto em armazenamento interno por uma allowlist fechada. Eventos aceitam somente nome versionado, etapa, resultado, tipo de dispositivo aproximado e identificadores técnicos internos. Valores, renda, patrimônio, títulos de meta, premissas numéricas, e-mail, texto livre e payloads são proibidos.

Feedback será estruturado por categoria, avaliação opcional e comentário curto explicitamente orientado a não conter dados financeiros. Suporte não terá painel administrativo com acesso amplo nesta fase; a operação será guiada por runbook e exportações minimizadas.

Uma feature flag interna controla a disponibilidade do beta. Habilitá-la não libera tráfego real: o checklist humano e a autorização manual de entrada continuam obrigatórios e separados da configuração técnica.

## Alternativas

- contratar analytics e help desk antes de validar o esquema mínimo;
- registrar URLs, payloads e propriedades arbitrárias;
- inferir métricas diretamente de tabelas financeiras;
- considerar a flag de beta como aprovação automática para produção;
- adiar colaboração compartilhada para depois do beta.

## Consequências

- consultas de aprendizagem são mais limitadas, mas auditáveis e minimizadas;
- um esquema fechado exige evolução explícita para cada novo evento;
- texto opcional de feedback ainda exige retenção, moderação e direito do titular;
- colaboração precisa de testes de autorização mais amplos antes do primeiro participante;
- fatores humanos continuam sendo um gate real, não um item substituído por testes automatizados.

## Riscos e mitigação

- dado financeiro em comentário: orientação visível, limite curto, retenção e acesso restrito;
- identificador usado para perfilamento: finalidade restrita, minimização e retenção curta;
- identificador de convite observado: resposta disponível somente na caixa autenticada do e-mail verificado, associação sob bloqueio transacional, uso único e expiração;
- operador confundir readiness com lançamento: checklist manual e estados distintos;
- assédio por convite: rate limit, auditoria e possibilidade de rejeição.

## Condições de revisão

Revisar antes de contratar analytics/help desk, adicionar session replay, anexos, segmentação comportamental, painel administrativo, mais de dois membros ou qualquer participante real.

## Aprovação

Conclusão técnica registrada em 30/08/2026. Aguardando aprovação explícita da Fase 8; o ADR permanece proposto até essa decisão.
