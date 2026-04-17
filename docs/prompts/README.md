# Guia de Prompts e Skills — TUCC

> Este documento explica como usar o sistema de prompts e skills do Claude Code neste projeto.

---

## Estrutura de Arquivos

```
tucc/
├── CLAUDE.md                          # Lido automaticamente pelo Claude Code em toda sessão
├── docs/
│   └── prompts/
│       ├── README.md                  # Este arquivo
│       └── system-prompt.md           # System prompt principal (Cole no início de sessões)
└── .claude/
    ├── commands/
    │   ├── add-table.md               # /add-table <NOME_TABELA>
    │   └── review.md                  # /review
    └── skills/
        ├── kafka-producer/SKILL.md    # Ativado automaticamente ao trabalhar no producer
        ├── kafka-consumer/SKILL.md    # Ativado automaticamente ao trabalhar no consumer
        └── db-sync/SKILL.md           # Ativado automaticamente ao trabalhar com entidades
```

---

## Como Usar

### CLAUDE.md (automático)
O Claude Code lê `CLAUDE.md` da raiz automaticamente em toda sessão. Contém:
- Stack tecnológica
- Estrutura dos módulos
- Regras críticas do projeto
- Convenções de commit

Você **não precisa fazer nada** — ele é carregado automaticamente.

### System Prompt (manual)
Use `docs/prompts/system-prompt.md` quando:
- Iniciar uma sessão longa no Claude.ai (não Claude Code)
- Configurar um agente externo com contexto do projeto
- Onboarding de novo desenvolvedor no time

**Como usar:** Copie o conteúdo e cole como system prompt ou primeira mensagem.

### Skills (automáticas)
As skills são ativadas pelo Claude Code automaticamente quando relevantes:

| Skill           | Quando ativa                                          |
|-----------------|-------------------------------------------------------|
| `kafka-producer`| Ao trabalhar no `tucc-sync-db-producer`               |
| `kafka-consumer`| Ao trabalhar no `tucc-sync-db-consumer`               |
| `db-sync`       | Ao criar/revisar entidades JPA ou mapeamentos         |

### Slash Commands (manuais)
Use digitando `/` no Claude Code:

| Comando              | O que faz                                             |
|----------------------|-------------------------------------------------------|
| `/add-table EMPRESA` | Gera checklist e código para adicionar nova tabela    |
| `/review`            | Revisa o código atual com as regras do TUCC           |

---

## Boas Práticas com Claude Code neste Projeto

### 1. Seja específico sobre o módulo
```
❌ "Crie o consumer"
✅ "No tucc-sync-db-consumer, implemente o handler para a tabela PRODUTO_EMPRESA"
```

### 2. Mencione a operação Kafka
```
✅ "Implemente idempotência para a operação DELETE no consumer da tabela EMPRESA"
```

### 3. Use /review antes de commitar
Execute `/review` para garantir que as regras críticas do projeto estão sendo seguidas.

### 4. Use subagentes para tarefas paralelas
Se precisar implementar suporte a uma nova tabela (producer + consumer + core), peça:
```
"Use subagentes para implementar em paralelo:
 1. O produtor CDC para a tabela CLIENTE
 2. O consumer e entidade JPA para CLIENTE  
 3. O endpoint GET /clientes no tucc-core"
```

### 5. Mantenha o CLAUDE.md atualizado
Ao adicionar novas entidades ou mudar regras, atualize `CLAUDE.md` e `system-prompt.md`.

---

## Evolução do Sistema de Prompts

| O que mudou             | Onde atualizar                    |
|-------------------------|-----------------------------------|
| Nova tabela/entidade    | `CLAUDE.md` + `system-prompt.md` + nova skill se necessário |
| Nova regra de negócio   | `CLAUDE.md` + `system-prompt.md`  |
| Novo módulo             | `CLAUDE.md` + nova skill          |
| Novo workflow frequente | Novo slash command em `.claude/commands/` |
