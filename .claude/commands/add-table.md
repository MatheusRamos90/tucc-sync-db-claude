---
description: Adiciona suporte completo a uma nova tabela Oracle no pipeline de sincronização TUCC. Chame com o nome da tabela, ex: /add-table CLIENTE
---

# Adicionar Nova Tabela ao Pipeline TUCC

Tabela a adicionar: $ARGUMENTS

## Passos

1. **Verifique** se a tabela já existe no Oracle (`oracle-init.sql`) e se tem `SUPPLEMENTAL LOG DATA ALL COLUMNS`. Se não existir, use `/monolito` para criar a tabela no legado primeiro.
2. **Migration Flyway** (`tucc-sync-db-consumer/src/main/resources/db/migration/`):
   - Crie o arquivo `V{proximo_numero}__{nome_tabela_lower}.sql`
   - Inclua o `CREATE TABLE` com os tipos corretos (BIGINT para IDs, NUMERIC(10,2) para monetários, VARCHAR para texto, TIMESTAMP para datas)
   - Adicione FKs se a tabela referenciar outras entidades sincronizadas
   - Nunca inclua `created_at`/`updated_at` se o legado Oracle não os tiver
3. **Producer** (`tucc-sync-db-producer`):
   - Adicione a tabela em `debezium.table.include.list` no `application.yml`
   - Crie ou atualize o mapeador Debezium para incluir a nova tabela
4. **Consumer** (`tucc-sync-db-consumer`):
   - Crie a entidade JPA com `@Entity` e `@Table(name = "nome_tabela")`
   - Crie o `Repository` com suporte a upsert
   - Adicione o handler no `SyncService` (switch/case na tabela)
5. **Core** (`tucc-core`):
   - Crie o endpoint GET de listagem com paginação
   - Documente com `@Operation` (Swagger)
6. **Testes**:
   - Teste de mapeamento Oracle → Payload
   - Teste de upsert (INSERT e UPDATE idempotente)
   - Teste de DELETE com registro inexistente (não deve lançar exceção)
7. **Atualize** `/docs/prompts/system-prompt.md` e `CLAUDE.md` com o mapeamento da nova entidade
