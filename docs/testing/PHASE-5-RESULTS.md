# Resultados de validação da Fase 5

## Automação

Executado em 28/08/2026 pelo script completo do projeto:

- OpenAPI 0.4 e tipos TypeScript regenerados;
- lint e TypeScript estrito;
- 10 testes frontend em 5 arquivos;
- build de produção do Next.js com dashboard e rota de cenários;
- 20 testes backend pelo Maven, sem falhas;
- migrations `V1` a `V4` em PostgreSQL 17.11 efêmero;
- JAR Maven empacotado com sucesso.

## Cenários e segurança

- comparação determinística contra a mesma base;
- cenário com prazo maior produz delta verificável;
- conjuntos vazios ou acima do limite são rejeitados;
- criação e leitura autenticadas por espaço;
- cenário de outro espaço retorna 404;
- snapshot de cenário possui hash e vínculo próprios;
- exportação LGPD inclui cenário e versões;
- regressão: criar cenário não altera a projeção base da meta.

## Frontend e QA visual

- dashboard e comparação exercitados com conta e valores exclusivamente sintéticos;
- desktop em 1440 × 900, tema claro;
- dashboard em 320 × 800, tema escuro;
- comparação em 320 × 800, tema claro;
- nenhum overflow global e nenhum erro de console;
- tabela possui caption, cabeçalhos de coluna e linha;
- gráfico de prazo possui nome acessível e equivalente tabular;
- tabela larga permanece em região rolável própria no móvel.

## Revisões estáticas

- nenhum `float` ou `double` no domínio financeiro;
- nenhuma taxa ou projeção default compilada no frontend;
- nenhum cálculo financeiro duplicado na UI;
- cenários não elegem opção recomendada;
- `git diff --check` sem erro.

## Riscos residuais

- o limite de três cenários é deliberado e poderá exigir arquivamento antes de crescer;
- validação matemática independente e pesquisa de compreensão continuam obrigatórias antes do beta;
- edição de plano base deverá gerar novo snapshot, nunca sobrescrever histórico.
