# Direção visual e design system

## Estado

Direção aprovada pelo usuário: identidade preto/branco, minimalista e moderna, com possibilidade de inversão pelo usuário. A referência a empresas como Apple é um padrão de rigor, clareza e acabamento, não autorização para copiar marca, componentes ou identidade.

## Conceito

**Precisão calma.** O produto apresenta números importantes como instrumentos de decisão, usando espaço, tipografia e linhas para construir confiança.

## Assinatura visual

**Linha de trajetória:** uma linha horizontal que conecta “hoje” à meta e responde a alterações de prazo e contribuição. Ela funciona como motivo recorrente em onboarding, detalhe da meta e comparação de cenários. Não é um gráfico decorativo; codifica tempo, progresso e desvio.

Essa assinatura evita que a identidade dependa apenas de um logotipo. Se a marca for removida, a linha de trajetória, a tipografia numérica e a composição editorial ainda devem tornar o produto reconhecível.

## Antipadrões vetados

- gradientes decorativos;
- glassmorphism;
- brilho neon;
- fundos roxos/azuis genéricos de IA;
- ilustrações 3D flutuantes;
- excesso de cards com sombra;
- pills e badges sem função;
- números enormes usados apenas para preencher espaço;
- microcopy técnica decorativa;
- animações espalhadas sem propósito;
- imitação de componentes, logotipo ou tipografia proprietária da Apple.

## Paleta — tema claro

| Token | Valor | Uso |
|---|---|---|
| `canvas` | `#FFFFFF` | fundo principal |
| `ink` | `#050505` | texto e ação principal |
| `surface` | `#F5F5F3` | campos e agrupamentos necessários |
| `muted` | `#666666` | texto secundário com contraste validado |
| `line` | `#D8D8D3` | divisores e estrutura |
| `inverse` | `#050505` | superfícies invertidas |

## Paleta — tema escuro

| Token | Valor | Uso |
|---|---|---|
| `canvas` | `#050505` | fundo principal |
| `ink` | `#FFFFFF` | texto e ação principal |
| `surface` | `#171717` | campos e agrupamentos necessários |
| `muted` | `#A7A7A7` | texto secundário |
| `line` | `#343434` | divisores e estrutura |
| `inverse` | `#FFFFFF` | superfícies invertidas |

Estados críticos usam texto, ícone e geometria, não apenas cor. A monocromia não elimina foco visível, sublinhado, padrões de linha ou labels.

## Tema

- Primeiro acesso respeita `prefers-color-scheme`.
- O usuário pode escolher `Claro`, `Escuro` ou `Sistema`.
- Preferência autenticada é persistida na conta; preferência local evita flash antes da sessão.
- Troca de tema não altera semântica, hierarquia ou leitura dos gráficos.
- Componentes e gráficos são testados nos dois temas.

## Tipografia

Duas famílias, ambas com licença e carregamento a validar na Fase 2:

- **Manrope:** interface, títulos e texto; geometria moderna sem copiar San Francisco.
- **IBM Plex Mono:** valores financeiros, datas-chave e comparações tabulares.

O mono é usado com restrição; parágrafos e rótulos permanecem em Manrope.

### Escala proposta

| Papel | Desktop | Mobile | Peso | Entrelinha |
|---|---:|---:|---:|---:|
| Display | 64 | 40 | 500 | 1.05 |
| H1 | 48 | 34 | 500 | 1.10 |
| H2 | 32 | 26 | 500 | 1.15 |
| H3 | 22 | 20 | 500 | 1.25 |
| Corpo | 16 | 16 | 400 | 1.50 |
| Utilitário | 14 | 14 | 500 | 1.40 |
| Nota | 12 | 12 | 400 | 1.45 |

Corpo mantém medida entre 45 e 75 caracteres. Valores usam algarismos tabulares. Headlines quebram em unidades semânticas e não deixam órfãs.

## Grid e espaçamento

- Base: 4 px.
- Escala: 4, 8, 12, 20, 32, 52 e 84.
- Desktop: 12 colunas, margem mínima de 6%, conteúdo até 1280 px.
- Tablet: 8 colunas.
- Mobile: 4 colunas, margem de 20 px.
- Formulários de decisão: largura de leitura, nunca ocupam a tela inteira sem necessidade.
- Espaçamento substitui containers sempre que comunica agrupamento.

## Forma

- Raios discretos: 8–12 px em controles; sem pílulas como padrão.
- Divisores de 1 px.
- Sombras ausentes na maior parte da interface; elevação apenas em popovers/modais.
- Uma ação primária por grupo.
- Ícones sempre acompanhados de label quando a ação não for universal.

## Composição

- Uma âncora principal por tela.
- Estrutura editorial assimétrica no desktop, com coluna de decisão e campo de projeção.
- No mobile, a projeção vem imediatamente após a entrada necessária.
- Negativo/espaço vazio separa etapas e reduz ansiedade.
- Nenhum dashboard de cartões com igual destaque.

## Motion

Um único gesto característico: a linha de trajetória se reposiciona ao comparar cenários. Duração curta, sem loop, com interpolação espacial e respeito a `prefers-reduced-motion`. Outras transições são funcionais e discretas.

## Componentes iniciais

- shell e seletor de espaço;
- campo monetário;
- campo de taxa com unidade explícita;
- seletor segmentado sem formato de pill excessivo;
- linha de trajetória;
- resumo de projeção;
- tabela de premissas;
- comparação de cenário;
- histórico de alterações;
- convite e lista de membros;
- confirmação destrutiva;
- theme switcher.

## Shell autenticado e dashboards

- A área autenticada usa sidebar persistente no desktop e drawer acionado explicitamente no mobile.
- A sidebar pode ser recolhida para ícones no desktop; rótulos completos permanecem disponíveis por `title` e nomes acessíveis.
- O item da rota atual usa `aria-current="page"` e contraste estrutural, sem depender apenas de cor.
- O seletor de tema, saída e contexto do espaço permanecem no rodapé e no topo da sidebar, respectivamente.
- Títulos da aplicação são mais compactos que displays de marketing para manter decisões e dados na primeira dobra.
- Dashboards evitam mosaicos de cards equivalentes: resumo, gráfico principal e linha do tempo possuem pesos distintos.
- Gráficos representam apenas resultados recebidos da API. Escalas calculadas no frontend servem exclusivamente à apresentação e sempre exibem os valores exatos em texto.
- Todo gráfico precisa de nome acessível, rótulos textuais e leitura equivalente sem depender da geometria das barras.

## Acessibilidade

- objetivo WCAG 2.2 AA;
- contraste de corpo mínimo 4,5:1 e alvo de 7:1;
- foco nunca removido;
- ordem DOM corresponde à leitura;
- zoom até 200% sem perda;
- targets próximos de 44 × 44 px no toque;
- inputs editáveis com pelo menos 16 px em mobile;
- erros associados a campo e resumo;
- gráficos com tabela/resumo textual;
- padrões de linha e labels distinguem cenários;
- motion reduzido preserva entendimento;
- tema escuro recebe testes próprios, não simples inversão automática de imagem.

## Brand-off test

Sem nome ou logotipo, a proposta ainda deve ser reconhecida pela combinação de:

1. linha de trajetória;
2. composição editorial de decisão + projeção;
3. valores em mono usados com moderação;
4. monocromia de alto contraste;
5. linguagem transparente e não promocional.
