# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Visão Geral

**TUCC** é um sistema de sincronização de dados entre um banco Oracle legado e PostgreSQL via Debezium CDC + Apache Kafka. Composto por três módulos Spring Boot independentes, cada um com seu próprio `pom.xml` e Maven wrapper.

```
Usuário (Browser)
       │
       ▼
tucc-monolito  (Java 8 / Struts 2 / JSP)  ← escrita via CRUD legado
       │  JDBC direto
       ▼
    Oracle ──── CDC via Debezium ──► tucc-sync-db-producer ──► Kafka (tucc-sync-event)
                                                                        │
                                                             tucc-sync-db-consumer
                                                                        │  UPSERT/DELETE
                                                                        ▼
Client (Web/Mobile) ──► tucc-core ───────────────────────────────── PostgreSQL
                                    Tópicos:
                                      tucc-sync-event.dlq   (falhas)
                                      tucc-sync-event.retry (reprocessamento)
```

**Regra de ouro:** O Oracle é escrito exclusivamente pelo `tucc-monolito`. Os microsserviços não escrevem no Oracle. Todo dado sincronizado para PostgreSQL vem pelo consumer Kafka.

---

## Módulos

| Módulo | Stack | Responsabilidade |
|--------|-------|-----------------|
| `tucc-monolito` | Java 8, Struts 2, JSP, JDBC | CRUD legado — única fonte de escrita no Oracle |
| `tucc-sync-db-producer` | Java 21, Spring Boot, Debezium | Captura CDC do Oracle e publica no Kafka |
| `tucc-sync-db-consumer` | Java 21, Spring Boot, Kafka, JPA | Consome do Kafka e persiste no PostgreSQL |
| `tucc-core` | Java 21, Spring Boot, JPA, REST | API REST somente GET — consulta o PostgreSQL |

---

## Comandos de Build e Teste

Cada módulo é construído e testado de forma independente.

**tucc-monolito** (Java 8, WAR, Tomcat 7 plugin):
```bash
cd tucc-monolito
mvn clean package            # gera tucc-monolito.war
mvn tomcat7:run              # sobe em http://localhost:8090
```

**Microsserviços** (Java 21, Spring Boot):

```bash
# Build
cd tucc-core && ./mvnw clean install
cd tucc-sync-db-producer && ./mvnw clean install
cd tucc-sync-db-consumer && ./mvnw clean install

# Testes
cd tucc-core && ./mvnw test
cd tucc-sync-db-producer && ./mvnw test
cd tucc-sync-db-consumer && ./mvnw test

# Executar localmente
cd tucc-core && ./mvnw spring-boot:run
cd tucc-sync-db-producer && ./mvnw spring-boot:run
cd tucc-sync-db-consumer && ./mvnw spring-boot:run
```

---

## Entidades e Mapeamento Oracle → PostgreSQL

### PRODUTO
| Oracle | PostgreSQL |
|--------|-----------|
| `ID NUMBER(10) IDENTITY` | `id BIGINT` (PK, sem `@GeneratedValue`) |
| `NOME VARCHAR2(255)` | `nome VARCHAR(255)` |
| `VALOR NUMBER(10,2)` | `valor NUMERIC(10,2)` |
| `DESCONTO NUMBER(10,2)` | `desconto NUMERIC(10,2)` |
| `DT_CRIACAO TIMESTAMP` | `created_at TIMESTAMP` |
| `DT_ATUALIZACAO TIMESTAMP` | `updated_at TIMESTAMP` |

### EMPRESA
| Oracle | PostgreSQL |
|--------|-----------|
| `ID NUMBER(10) IDENTITY` | `id BIGINT` (PK, sem `@GeneratedValue`) |
| `NOME VARCHAR2(255)` | `nome VARCHAR(255)` |
| `CNPJ VARCHAR2(18)` | `cnpj VARCHAR(18)` |
| `DT_CRIACAO TIMESTAMP` | `created_at TIMESTAMP` |
| `DT_ATUALIZACAO TIMESTAMP` | `updated_at TIMESTAMP` |

### PRODUTO_EMPRESA (tabela de associação)
| Oracle | PostgreSQL |
|--------|-----------|
| `ID NUMERIC(10)` | `id BIGINT` (PK, sem `@GeneratedValue`) |
| `PRODUTO_ID NUMERIC(10)` | `produto_id BIGINT` (FK) |
| `EMPRESA_ID NUMERIC(10)` | `empresa_id BIGINT` (FK) |

> Sem `created_at`/`updated_at` — o legado não possui esses campos nesta tabela.

---

## Regras Críticas

### Consumer (`tucc-sync-db-consumer`)
- **Idempotência obrigatória:** use UPSERT para INSERT/UPDATE; para DELETE, não lance exceção se o registro não existir
- **DLQ:** toda falha não-recuperável envia a mensagem para `tucc-sync-event.dlq` com stack trace completo
- **Nunca commite o offset Kafka antes de confirmar a persistência no PostgreSQL**
- Use `@Transactional` apenas onde necessário, sempre com rollback garantido

### Producer (`tucc-sync-db-producer`)
- Payload Kafka: `{ "table": "PRODUTO", "operation": "INSERT|UPDATE|DELETE", "data": {...} }`
- Nomes de tópicos: use constantes ou `@Value` — nunca hardcode

### Core (`tucc-core`)
- Endpoints de **dados sincronizados do Oracle** são somente GET — nenhuma escrita nesses recursos
- Funcionalidades novas sem relação com o sync podem usar qualquer método HTTP (POST, PUT, DELETE)
- Use paginação em listagens
- Documente com Swagger/OpenAPI

### JPA (todos os módulos)
- PKs do Oracle são PKs no PostgreSQL — **nunca use `@GeneratedValue`**
- Relacionamentos: sempre `@ManyToOne(fetch = FetchType.LAZY)` com `insertable = false, updatable = false` quando a FK já está mapeada como campo `Long`
- Campos de data do Oracle (`DT_CRIACAO`, `DT_ATUALIZACAO`) → `created_at`/`updated_at` **somente se existirem no legado**

---

## Estilo de Código

```java
// Payloads Kafka: records Java 21 imutáveis
public record SyncEventPayload(
    String table,
    String operation,
    Map<String, Object> data,
    Instant capturedAt
) {}

// Consumer: idempotência com switch expression
public void processSyncEvent(SyncEventPayload event) {
    switch (event.operation()) {
        case "INSERT", "UPDATE" -> upsert(event.data());
        case "DELETE" -> deleteIfExists(event.data());
        default -> log.warn("Operação desconhecida: {}", event.operation());
    }
}
```

---

## Slash Commands Disponíveis

| Comando | Uso |
|---------|-----|
| `/add-table NOME_TABELA` | Gera checklist e código para adicionar nova tabela Oracle ao pipeline |
| `/review` | Revisa o código com foco em idempotência, DLQ e ausência de escrita no Oracle |

---

## tucc-monolito — Detalhes do Legado

Stack: Java 8 · Struts 2.5 · JSP · JSTL · Bootstrap 3 · jQuery · Apache DBCP · Oracle JDBC

**Estrutura interna:**
- `action/` — Struts 2 Actions (implementam `SessionAware`)
- `dao/` — JDBC puro via Apache DBCP 1.x (`ConnectionFactory` + DAOs por entidade)
- `model/` — POJOs Java com getters/setters
- `converter/` — type converters Struts 2 (ex: `BigDecimalConverter`)
- `webapp/WEB-INF/jsp/` — views JSP organizadas por entidade
- `resources/xwork-conversion.properties` — registra converters globais do Struts 2

**Conexão Oracle:**
- Env vars: `ORACLE_HOST`, `ORACLE_PORT`, `ORACLE_DB` (padrão: XEPDB1), `ORACLE_USER`, `ORACLE_PASSWORD`
- Pool: `BasicDataSource` do Commons DBCP 1.4
- URL: `jdbc:oracle:thin:@//${ORACLE_HOST}:${ORACLE_PORT}/${ORACLE_DB}`

**IDs:** gerados pelo Oracle via `GENERATED ALWAYS AS IDENTITY` — nunca inseridos pelo Java

**Porta:** 8090 local / 8090:8080 no Docker

**BigDecimal em formulários:** O JVM pode rodar com locale `pt_BR`, onde `.` é separador de milhar. Para evitar que `5200.97` seja parseado como `520097`, existe `BigDecimalConverter` registrado globalmente em `xwork-conversion.properties`. Aceita tanto `.` quanto `,` como separador decimal.

---

## Armadilhas Conhecidas

| Problema | Causa | Solução |
|----------|-------|---------|
| `BigDecimal` com valor errado no monolito | Struts 2 usa `NumberFormat` locale `pt_BR` — `.` vira separador de milhar | `BigDecimalConverter` em `xwork-conversion.properties` |
| `ClassCastException: String cannot be cast to Number` no consumer | `decimal.handling.mode=string` no Debezium — todo campo numérico Oracle chega como `String` no Kafka | `toLong`/`toBigDecimal` no `SyncService` aceitam `String` e `Number` |
| Timestamp com ano > 2200 no PostgreSQL | Oracle `TIMESTAMP` chega do Debezium em **microsegundos** desde epoch, não milissegundos | `toInstant` no `SyncService`: `Instant.ofEpochSecond(0, micros * 1000L)` |
| `No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper'` no consumer | Spring Boot 4 auto-configura Jackson 3.x (`tools.jackson`); Spring Kafka precisa de Jackson 2.x (`com.fasterxml.jackson`) | `JacksonConfig` com `@Bean("jackson2ObjectMapper")` + `@Qualifier` em `KafkaConsumerConfig` |

---

## Infraestrutura Docker

| Serviço | Porta | Acesso |
|---------|-------|--------|
| `tucc-monolito` | 8090 | http://localhost:8090 |
| `tucc-core` | 8080 | http://localhost:8080 |
| `tucc-sync-db-consumer` | 8081 | http://localhost:8081 |
| `kafka-ui` (Provectus) | 8082 | http://localhost:8082 |
| `postgres` | 5432 | jdbc:postgresql://localhost:5432/tucc |
| `oracle-xe` | 1521 | jdbc:oracle:thin:@//localhost:1521/XEPDB1 |
| `kafka` | 9092 | localhost:9092 |

---

## Ao Adicionar Nova Entidade

Atualize este arquivo (`CLAUDE.md`) **e** `docs/prompts/system-prompt.md` com o mapeamento de tipos da nova tabela.

Quando a entidade **nasce no monolito** (nova tela/tabela Oracle), também é preciso atualizar toda a camada de sincronização — use o comando `/add-table NOME_TABELA` e a skill `/monolito` para o checklist completo.
