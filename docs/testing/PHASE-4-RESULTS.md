# Resultados de validação da Fase 4

## Automação

Executado em 28/08/2026:

- geração dos tipos TypeScript a partir do OpenAPI 0.3;
- lint e TypeScript estrito;
- 8 testes frontend em 4 arquivos;
- build de produção do Next.js;
- 17 testes backend pelo Maven, sem falhas;
- migrations `V1` a `V3` em PostgreSQL 17.11 efêmero;
- empacotamento do JAR com Maven.

## Motor e persistência

- taxa zero e soma linear;
- valor em moeda corrente atualizado por inflação;
- meta já financiada;
- aporte no início versus fim do mês;
- retorno negativo e capacidade declarada;
- data-alvo inválida;
- snapshot reproduzível e hash canônico;
- isolamento entre espaços, CSRF e contribuição idempotente;
- conflito quando a chave idempotente é reutilizada com outro conteúdo;
- exportação LGPD com meta, contribuição e versão da fórmula.

## QA visual e acessibilidade

Jornada real executada localmente com conta e valores sintéticos, frontend em `localhost:3000`, API Maven em `localhost:8080` e PostgreSQL local.

- criação e detalhe inspecionados em 1440 × 900;
- detalhe inspecionado em 320 × 800;
- temas escuro e claro validados pelo controle da interface;
- hierarquia de títulos, landmarks, rótulos e mensagens presentes no DOM acessível;
- nenhum erro ou aviso de console;
- nenhum overflow horizontal;
- truncamento móvel inicial nos valores das premissas corrigido e revalidado.

## Revisões estáticas

- nenhuma regra financeira encontrada no frontend;
- nenhum `float` ou `double` no código financeiro;
- taxas, URLs externas, moedas e segredos não foram introduzidos como defaults de domínio;
- `git diff --check` sem erro.

## Risco residual

Os casos matemáticos possuem testes determinísticos, mas a validação independente por especialista financeiro permanece necessária antes do beta.
