# TUCC Sync DB

Sistema de sincronização de dados entre um banco Oracle legado e PostgreSQL usando Debezium CDC + Apache Kafka.

## Arquitetura

```
tucc-monolito (Java 8 / Struts 2)
       │ escrita JDBC
       ▼
    Oracle ──── Debezium CDC ──► tucc-sync-db-producer ──► Kafka
                                                                │
                                                   tucc-sync-db-consumer
                                                                │ UPSERT / DELETE
                                                                ▼
                               tucc-core (REST API) ────── PostgreSQL
```

| Módulo | Responsabilidade |
|--------|-----------------|
| `tucc-monolito` | CRUD legado — única fonte de escrita no Oracle |
| `tucc-sync-db-producer` | Captura CDC via Debezium e publica no Kafka |
| `tucc-sync-db-consumer` | Consome do Kafka e persiste no PostgreSQL |
| `tucc-core` | API REST somente leitura sobre o PostgreSQL |

## Como rodar

```bash
docker compose up -d
```

Serviços disponíveis após o boot:

| Serviço | URL |
|---------|-----|
| Monolito | http://localhost:8090 |
| tucc-core API | http://localhost:8080 |
| Kafka | localhost:9092 |
| PostgreSQL | localhost:5432 |
| Oracle XE | localhost:1521 |

## Desenvolvimento

Cada módulo Java 21 tem seu próprio Maven wrapper:

```bash
cd tucc-core && ./mvnw test
cd tucc-sync-db-producer && ./mvnw test
cd tucc-sync-db-consumer && ./mvnw test
```

O `tucc-monolito` usa Maven direto (Java 8):

```bash
cd tucc-monolito && mvn clean package
```
