---
description: Revisa o código alterado com foco nas regras do TUCC: idempotência, DLQ, mapeamento correto e ausência de escrita no Oracle. Use antes de abrir um PR.
---

# Code Review TUCC

Revise as alterações atuais (`git diff`) com os seguintes critérios:

## 1. Regras de Negócio
- [ ] Nenhuma escrita no Oracle (apenas leitura via CDC)
- [ ] `tucc-core` não possui endpoints de escrita (POST/PUT/DELETE) para dados sincronizados do Oracle — funcionalidades novas sem relação com o sync podem usar qualquer método HTTP
- [ ] Sincronização passa exclusivamente pelo consumer Kafka

## 2. Kafka / Consumer
- [ ] Offset só é commitado após persistência confirmada no Postgres
- [ ] Falhas vão para DLQ com log detalhado
- [ ] Idempotência garantida (INSERT/UPDATE usam upsert, DELETE é tolerante)

## 3. Qualidade de Código
- [ ] Sem `double` para valores monetários (use `BigDecimal`)
- [ ] IDs do Oracle preservados (sem `@GeneratedValue`)
- [ ] Timestamps preservados (sem `LocalDateTime.now()` para campos do Oracle)
- [ ] Tópicos Kafka via `@Value` ou constantes (sem hardcode)

## 4. Monolito → Microsserviços
- [ ] Se adicionou campo no Oracle: Flyway migration + entidade consumer + DTO core atualizados
- [ ] Se adicionou nova tabela: `SUPPLEMENTAL LOG DATA ALL COLUMNS` no DDL Oracle + pipeline completo (`/add-table`)
- [ ] Novos campos `BigDecimal` no form Struts 2 não precisam de converter manual — `BigDecimalConverter` já é global
- [ ] `SyncService` aceita valores numéricos como `String` (Debezium `decimal.handling.mode=string`)
- [ ] Timestamps mapeados como microssegundos → `Instant.ofEpochSecond(0, micros * 1000L)`

## 4. Testes
- [ ] Lógica de mapeamento coberta por testes unitários
- [ ] Cenário de DLQ testado
- [ ] Cenário de idempotência testado

Aponte problemas encontrados, sugira correções e dê uma nota geral (APROVADO / AJUSTES NECESSÁRIOS / REPROVADO).
