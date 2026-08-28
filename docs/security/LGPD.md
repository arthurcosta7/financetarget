# Plano inicial de LGPD e privacidade

## Aviso

Este documento traduz requisitos de produto e engenharia. Não é parecer jurídico. Bases legais, prazos, textos e papéis devem ser confirmados por profissional qualificado antes do beta.

## Princípios

- finalidade clara;
- minimização;
- necessidade;
- transparência;
- segurança;
- prevenção;
- não discriminação;
- responsabilização demonstrável.

## Inventário inicial

| Categoria | Exemplos | Finalidade proposta | Retenção proposta | Sensibilidade operacional |
|---|---|---|---|---|
| Identidade | nome, e-mail | conta e comunicação essencial | enquanto conta ativa + prazo aprovado | Alta |
| Segurança | hash, sessão, IP parcial, eventos | autenticação e prevenção a abuso | curta e definida por risco | Alta |
| Perfil financeiro | renda e despesas agregadas | calcular capacidade | enquanto necessário ao serviço | Muito alta |
| Metas | título, valor, prazo | planejamento | enquanto meta/conta ativa | Muito alta |
| Colaboração | associação, convite, papéis | metas compartilhadas | enquanto vínculo ou obrigação aplicável | Alta |
| Projeções | premissas e snapshots | explicar e reproduzir cálculo | enquanto histórico for necessário | Muito alta |
| Consentimentos | versão e decisão | demonstrar escolha | prazo jurídico aprovado | Alta |
| Auditoria | ator, ação, resultado | segurança e responsabilização | proporcional ao risco | Alta |
| Suporte | mensagens e anexos futuros | atendimento | política específica | Variável |

Não coletar CPF, conta bancária, endereço residencial ou transações no MVP manual sem necessidade aprovada.

## Base legal

A base deve ser definida por finalidade, não por tabela. Execução de contrato, legítimo interesse, obrigação legal e consentimento são hipóteses a confirmar. Consentimento não será usado como autorização genérica para todo tratamento.

## Direitos do titular

O produto deverá oferecer:

- confirmação e acesso;
- correção;
- exportação compreensível e estruturada;
- informação sobre compartilhamento;
- revogação quando o tratamento depender de consentimento;
- oposição ou revisão aplicável;
- solicitação de exclusão;
- canal de contato.

Solicitações sensíveis exigem autenticação forte ou verificação adicional. O processo não deve expor a existência de contas a terceiros.

## Espaços compartilhados

Dados de um espaço podem envolver mais de um titular.

- O usuário vê claramente o que será compartilhado antes de aceitar convite.
- Perfil financeiro privado não é compartilhado automaticamente.
- Exportação pessoal exclui dados privados de outro membro.
- Exclusão de conta não pode apagar unilateralmente dados que pertencem ao parceiro.
- Autoria histórica pode ser pseudonimizada quando a pessoa sair.
- A regra final para contribuições atribuídas, histórico e exclusão conjunta requer decisão de produto e revisão jurídica.

## Consentimento e preferências

Separar:

- termos necessários ao serviço;
- aviso de privacidade;
- comunicações essenciais;
- marketing opcional;
- conexão de integração específica;
- uso opcional de dados para pesquisa ou melhoria.

Cada registro contém versão do texto, finalidade, decisão, momento e origem. Revogação deve ser tão acessível quanto adesão.

## Retenção e exclusão

Fluxo proposto:

1. receber solicitação;
2. autenticar solicitante;
3. identificar dados pessoais, compartilhados, obrigações e riscos;
4. suspender usos não necessários quando aplicável;
5. excluir, anonimizar ou reter justificadamente;
6. propagar a operadores;
7. informar conclusão e exceções;
8. manter evidência mínima da solicitação.

Prazos e exceções permanecem pendentes de validação jurídica.

## Exportação

- arquivo criptografado ou link de vida curta;
- reautenticação recente;
- dados legíveis e estruturados;
- nenhum segredo, hash, token ou dado privado do parceiro;
- expiração e auditoria do download;
- geração assíncrona futura com status claro.

Na implementação atual, a exportação autenticada inclui os dados da conta, perfil, consentimentos, solicitações, metas e cenários criados pelo titular, versões do motor/fórmula e contribuições de sua autoria. Ela não inclui hashes, tokens nem dados privados de outro membro. Entrega criptografada ou por link temporário continua fora do escopo local e é obrigatória antes da operação real.

## Operadores e transferências

Antes de contratar serviço externo, registrar:

- finalidade;
- categorias de dados;
- região de processamento;
- retenção;
- suboperadores;
- segurança;
- exclusão;
- resposta a incidente;
- transferência internacional;
- mecanismo contratual aplicável.

## Analytics

Eventos não incluem renda, patrimônio, texto livre de meta, título personalizado ou valores de projeção. Identificadores analíticos devem ser minimizados e separados quando possível.

## Incidente

- conter e preservar evidências;
- determinar categorias, titulares e impacto;
- envolver responsável jurídico e de segurança;
- avaliar notificações obrigatórias;
- corrigir, monitorar e registrar causa raiz;
- revisar controles e RIPD.

## RIPD

Avaliar formalmente a necessidade antes do beta, considerando perfil financeiro, colaboração, escala, integrações e potenciais efeitos de decisões baseadas em projeções.

## Questões para revisão qualificada

1. papel exato da empresa como agente de tratamento;
2. bases legais por finalidade;
3. prazos de retenção;
4. exclusão em espaços compartilhados;
5. tratamento de dados inferidos pelo motor;
6. requisitos de comunicação de incidente;
7. transferências internacionais;
8. textos de termos, privacidade e avisos financeiros;
9. eventual enquadramento regulatório além da LGPD.
