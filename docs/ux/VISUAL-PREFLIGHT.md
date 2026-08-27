# Preflight visual da Fase 1

Este é um preflight da especificação e dos wireframes, não aprovação visual final. Nenhuma interface renderizada de produção existe para inspeção.

## Locks

- Preto/branco e inversão pelo usuário: aprovado na especificação.
- Minimalismo moderno: traduzido em grid, tipografia, espaço e ausência de efeitos decorativos.
- Não parecer interface genérica de IA: gradientes, glassmorphism, glow e card grid vetados.
- Acessibilidade: WCAG 2.2 AA e significado independente de cor.

## Gates

| Gate | Estado | Evidência |
|---|---|---|
| Mensagem | Passa no plano | Uma mensagem primária por tela |
| Hierarquia | Passa no plano | Uma âncora dominante por wireframe |
| Estrutura | Passa no plano | Grid e alinhamentos definidos |
| Tipografia | Passa no plano | Dois papéis tipográficos e medidas definidas |
| Cor | Passa no plano | Tokens claros/escuros e contraste a testar |
| Marca | Parcial | Assinatura definida; nome/logotipo permanecem abertos |
| Acessibilidade | Passa no plano | Critérios registrados; execução ainda não testada |
| Anti-slop | Passa no plano | Lista de vetos explícita |

## Brand-off test

O conceito não depende apenas do nome: linha de trajetória, composição editorial, valores monoespaçados e linguagem de premissas formam uma identidade candidata. A diferenciação precisa ser confirmada em protótipo renderizado.

## Pendências para QA real

- contraste medido em componentes reais;
- screenshots em claro/escuro, desktop/mobile;
- teste de um segundo, thumbnail, blur e escala de cinza;
- foco e navegação por teclado;
- reflow a 320 px e zoom de 200%;
- motion reduzido;
- avaliação de aparência genérica após implementação.

Não há signoff visual final nesta fase.
