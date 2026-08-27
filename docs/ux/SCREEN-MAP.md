# Mapa de telas e navegação

## Público

```text
/
├── como-funciona
├── privacidade
├── termos
├── entrar
├── cadastro
├── verificar-email
└── recuperar-acesso
```

## Aplicação autenticada

```text
/app
├── inicio
├── onboarding
│   ├── boas-vindas
│   ├── escolher-espaco
│   ├── capacidade
│   └── primeira-meta
├── metas
│   ├── nova
│   └── {goalId}
│       ├── visao-geral
│       ├── cenarios
│       ├── progresso
│       ├── historico
│       └── configuracoes
├── espaco
│   ├── membros
│   ├── convidar
│   └── perfil-financeiro
└── configuracoes
    ├── conta
    ├── aparencia
    ├── notificacoes
    ├── privacidade-e-dados
    └── sessoes
```

## Navegação principal

Desktop:

- logomarca provisória;
- seletor do espaço;
- Início;
- Metas;
- Espaço;
- Configurações.

Mobile:

- topo com contexto do espaço;
- navegação inferior: Início, Metas e Espaço;
- Configurações no menu da conta.

Não duplicar todas as rotas na navegação. Cenários, progresso e histórico pertencem ao contexto de uma meta.

## Fluxos críticos

### Primeira meta pessoal

```mermaid
flowchart LR
    A[Cadastro] --> B[Verificação]
    B --> C[Espaço pessoal]
    C --> D[Capacidade mensal]
    D --> E[Criar meta]
    E --> F[Ver projeção]
    F --> G[Comparar cenário]
    G --> H[Salvar plano]
```

### Meta compartilhada

```mermaid
flowchart LR
    A[Criar espaço] --> B[Convidar parceiro]
    B --> C[Aceite informado]
    C --> D[Definir agregado compartilhado]
    D --> E[Criar meta]
    E --> F[Comparar cenários]
    F --> G[Acompanhar contribuições]
```

### Revisão mensal

```mermaid
flowchart LR
    A[Início] --> B[Meta]
    B --> C[Registrar contribuição]
    C --> D[Atualizar progresso]
    D --> E[Recalcular]
    E --> F[Manter ou ajustar cenário]
```
