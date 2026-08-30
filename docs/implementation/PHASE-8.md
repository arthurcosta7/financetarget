# Fase 8 — Beta fechado

## Objetivo

Preparar uma experiência de beta fechado mensurável, assistível e segura, completando o fluxo compartilhado exigido pelo MVP e demonstrando os controles com dados exclusivamente sintéticos antes de qualquer entrada de usuário real.

## Escopo autorizado

- espaços compartilhados para casal, convite vinculado ao destinatário e papéis `OWNER`, `EDITOR` e `VIEWER`;
- seleção explícita do espaço ativo e perfil financeiro agregado separado do perfil pessoal;
- autorização por associação ativa e papel, com testes negativos contra IDOR;
- histórico/auditoria das mudanças sensíveis de colaboração;
- eventos analíticos internos com allowlist, sem valores, títulos de meta, texto livre, e-mail ou renda;
- feedback estruturado do beta, com categoria e avaliação, sem anexos;
- canal e runbook de suporte, triagem e incidente;
- critérios de aprendizagem, painel operacional e checklist de entrada/saída do beta;
- revisão de contrato OpenAPI, exportação do titular, threat model, LGPD e UX.

## Fora do escopo

- convidar participantes reais, importar ou tratar dados financeiros reais;
- deploy, domínio público ou abertura de inscrições;
- plataforma externa de analytics, suporte ou produto;
- aprovação jurídica, elaboração de parecer ou RIPD definitivo;
- validação matemática independente por especialista;
- pagamentos, Open Finance ou outras integrações reais;
- remoção definitiva de membro ou exclusão unilateral de conteúdo compartilhado enquanto a regra jurídica estiver aberta.

## Critérios de aceite técnico

| Critério | Evidência exigida |
|---|---|
| Convite não pode ser aceito por outra conta | caixa autenticada vinculada ao e-mail verificado, identificador não secreto, expiração, uso único e teste negativo |
| Papéis limitam operações | testes para `OWNER`, `EDITOR` e `VIEWER` em recursos do espaço |
| Dados pessoais não vazam para o casal | perfil agregado criado explicitamente e exportação escopada ao titular |
| Telemetria não recebe dados financeiros | enum fechado de eventos/propriedades, rejeição de chaves desconhecidas e testes |
| Feedback evita coleta livre desnecessária | categorias e nota estruturadas; comentário opcional limitado e aviso de não incluir dados financeiros |
| Jornada funciona em celular e desktop | inspeção visual, teclado, foco, temas e ausência de overflow |
| Operação do beta é interrompível | feature flag, checklist de entrada, critérios de pausa e runbook |
| Gate externo permanece explícito | status técnico não é confundido com autorização para pessoas ou dados reais |

## Gates humanos antes do primeiro participante

1. revisão jurídica de termos, privacidade, colaboração, retenção e transferência do Resend;
2. RIPD e responsáveis por privacidade/incidente definidos;
3. validação independente do Goal Engine e das mensagens de projeção;
4. domínio de e-mail com SPF, DKIM e DMARC, além de monitoramento de bounce/complaint;
5. ambiente externo isolado, cofre, TLS, alertas e agenda de backup autorizados;
6. roteiro de recrutamento, consentimento de pesquisa e capacidade de suporte aprovados;
7. aprovação manual do checklist de entrada do beta.

## Critérios de aprendizagem

- ativação: cadastro verificado → perfil → primeira meta → primeiro cenário;
- compreensão: pessoa identifica que projeção não é garantia e explica a principal premissa;
- colaboração: convite aceito e primeira meta compartilhada sem exposição inesperada;
- recorrência: retorno e atualização manual em 7 e 30 dias;
- confiança: avaliação estruturada e motivo de abandono;
- continuidade: prosseguir somente se não houver incidente crítico, falha matemática material ou padrão de interpretação como promessa.

## Gate da fase

Ao final, a preparação técnica pode ser marcada como concluída, mas o beta permanece fechado para usuários reais até todos os gates humanos estarem registrados como aprovados. Parar e solicitar autorização específica antes de deploy, recrutamento ou tratamento de dados reais.
