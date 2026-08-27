# Wireframes de baixa fidelidade

Wireframes estruturais, não layouts finais. `█` representa ação principal e `─` representa a linha de trajetória.

## 1. Landing page — desktop

```text
┌──────────────────────────────────────────────────────────────────────┐
│ PLANEJAMENTO        Como funciona   Privacidade          Entrar      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│ Descubra o caminho                                                   │
│ para realizar sua meta.                  Hoje ───────●────── Meta    │
│                                                                      │
│ Compare prazo e aporte com                                           │
│ premissas que você entende.              R$ 2.180 / mês              │
│                                                                      │
│ [ █ Criar meu primeiro plano ]          Estimativa, não garantia     │
│                                                                      │
├──────────────────────────────────────────────────────────────────────┤
│ Uma meta → suas premissas → cenários → acompanhamento                │
└──────────────────────────────────────────────────────────────────────┘
```

Âncora: headline. Assinatura: linha de trajetória como demonstração, não gráfico decorativo.

## 2. Onboarding — capacidade

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Etapa 2 de 3                                      Salvar e sair      │
│                                                                      │
│ Quanto pode ir para suas metas?                                      │
│                                                                      │
│ Renda mensal do espaço          [ R$ __________ ]                    │
│ Despesas essenciais             [ R$ __________ ]                    │
│                                                                      │
│ Capacidade estimada             R$ 2.400 / mês                       │
│ Você decide o valor usado       [ R$ __________ ]                    │
│                                                                      │
│ Este valor será visível para Ana e Caio neste espaço.                │
│                                               [ █ Continuar ]        │
└──────────────────────────────────────────────────────────────────────┘
```

## 3. Início — espaço compartilhado

```text
┌──────────────┬───────────────────────────────────────────────────────┐
│ PLANEJAMENTO │ Casa 2029 ▾                         Claro / Escuro     │
│              ├───────────────────────────────────────────────────────┤
│ Início       │ Bom dia.                                              │
│ Metas        │                                                       │
│ Espaço       │ Entrada do imóvel                                     │
│              │ R$ 42.800 de R$ 120.000                               │
│              │ Hoje ─────────────●──────────────── Meta              │
│              │                                                       │
│              │ Próxima ação                                          │
│              │ Registrar contribuição de agosto                      │
│              │ [ █ Registrar ]     [ Ver plano ]                     │
│              │                                                       │
│ Configurações│ Alterado por Ana há 3 dias                            │
└──────────────┴───────────────────────────────────────────────────────┘
```

Uma meta domina a tela. Outras metas aparecem abaixo como lista, não como cards concorrentes.

## 4. Detalhe da meta

```text
┌──────────────────────────────────────────────────────────────────────┐
│ ← Metas                 Entrada do imóvel             Editar         │
├──────────────────────────────┬───────────────────────────────────────┤
│ Seu plano                    │ Projeção                              │
│                              │                                      │
│ Meta          R$ 120.000     │ Hoje ─────────●──────────── Meta      │
│ Data          mar 2030       │                                      │
│ Saldo         R$ 42.800      │ R$ 2.180 por mês                     │
│ Aporte        R$ 2.180       │ para chegar em março de 2030         │
│                              │                                      │
│ [ █ Ajustar cenário ]        │ Premissas: inflação  · retorno · ... │
├──────────────────────────────┴───────────────────────────────────────┤
│ Progresso  |  Cenários  |  Histórico                               │
└──────────────────────────────────────────────────────────────────────┘
```

## 5. Comparação de cenários

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Compare as escolhas                                                  │
│                                                                      │
│                 Base        Mais prazo       Mais aporte             │
│ Aporte/mês      R$ 2.180    R$ 1.720         R$ 2.700               │
│ Data            mar 2030    mar 2031         abr 2029                │
│ Total próprio   ...         ...              ...                     │
│                                                                      │
│ Base        Hoje ───────────●──────── Meta                           │
│ Mais prazo  Hoje ──────────────────●── Meta                           │
│ Mais aporte Hoje ────────●──────────── Meta                           │
│                                                                      │
│ [ Usar como cenário-base ]                 [ █ Salvar comparação ]   │
│ Projeções dependem das premissas informadas.                         │
└──────────────────────────────────────────────────────────────────────┘
```

Linhas usam padrões e labels, não apenas cor.

## 6. Espaço e convite

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Espaço compartilhado                                                 │
│                                                                      │
│ Casa 2029                                                            │
│ Ana             Proprietária                [ Alterar papel ]        │
│ Caio            Proprietário                [ Alterar papel ]        │
│                                                                      │
│ [ █ Convidar pessoa ]                                                │
│                                                                      │
│ O convite dá acesso às metas, cenários e valores deste espaço.       │
│ Dados do espaço pessoal de cada pessoa não são compartilhados.       │
└──────────────────────────────────────────────────────────────────────┘
```

## 7. Aparência e privacidade

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Configurações                                                        │
│                                                                      │
│ Aparência                                                            │
│ Tema     (•) Sistema   ( ) Claro   ( ) Escuro                        │
│                                                                      │
│ Seus dados                                                           │
│ [ Solicitar exportação ]                                             │
│ [ Excluir minha conta ]                                              │
│                                                                      │
│ A exclusão não remove automaticamente dados compartilhados de       │
│ outras pessoas. O impacto será mostrado antes da confirmação.        │
└──────────────────────────────────────────────────────────────────────┘
```

## Reflow mobile

- Sidebar vira navegação inferior.
- Colunas “Seu plano” e “Projeção” empilham, mantendo a projeção antes dos detalhes avançados.
- Tabela de cenários vira lista por atributo, preservando comparação.
- Ação primária ocupa a largura disponível; ações secundárias permanecem textuais.
- Linha de trajetória reduz detalhes, não o tamanho dos labels.
