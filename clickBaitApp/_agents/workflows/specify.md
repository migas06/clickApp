---
description: Speckit — sistema de especificação de features. Mostra todos os sub-comandos disponíveis ou delega para o sub-comando indicado.
---

## Uso

`/specify [sub-comando] [argumentos]`

Se não for passado nenhum sub-comando, apresenta a lista de comandos disponíveis.

## Comandos disponíveis

| Comando | Descrição |
|---------|-----------|
| `/specify.constitution` | Cria ou atualiza a constituição do projeto |
| `/specify.specify` | Cria ou atualiza a especificação de uma feature a partir de uma descrição em linguagem natural |
| `/specify.clarify` | Identifica áreas subespicificadas na spec ativa e faz até 5 perguntas de clarificação |
| `/specify.plan` | Executa o planeamento técnico da implementação com base na spec |
| `/specify.tasks` | Gera um `tasks.md` ordenado por dependências com base nos artefactos de design disponíveis |
| `/specify.checklist` | Gera uma checklist de qualidade de requisitos para a feature ativa |
| `/specify.analyze` | Análise de consistência entre `spec.md`, `plan.md` e `tasks.md` (apenas leitura) |
| `/specify.implement` | Executa a implementação processando todas as tasks em `tasks.md` |
| `/specify.taskstoissues` | Converte as tasks em GitHub Issues ordenadas por dependências |

## Fluxo recomendado

```
/specify.constitution   → define os princípios do projeto (uma vez)
/specify.specify        → cria a spec da feature
/specify.clarify        → (opcional) clarifica ambiguidades
/specify.plan           → cria o plano técnico
/specify.checklist      → (opcional) valida a qualidade dos requisitos
/specify.tasks          → gera as tasks de implementação
/specify.analyze        → (opcional) verifica consistência entre artefactos
/specify.implement      → executa a implementação
/specify.taskstoissues  → (opcional) cria GitHub Issues
```

## Passos

1. Se o utilizador escreveu `/specify` sem argumentos, apresenta a tabela acima e o fluxo recomendado. Para aqui.

2. Se o utilizador escreveu `/specify [sub-comando] [argumentos]`, delega imediatamente para o workflow `_agents/workflows/specify.[sub-comando].md` com os argumentos fornecidos. Lê esse ficheiro e segue as instruções nele definidas.
