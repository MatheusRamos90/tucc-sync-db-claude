# tucc-sync-db-consumer

Microsserviço responsável por consumir eventos do **Apache Kafka** e sincronizá-los no **PostgreSQL**, aplicando UPSERT ou DELETE conforme a operação recebida.

![Diagrama de arquitetura](../docs/arch/tucc-sync-db-claude.png)

## Como funciona

1. O **`SyncEventConsumer`** escuta o tópico `tucc-sync-event` com ack manual.
2. O **`SyncService`** identifica a tabela e a operação e persiste via JPA de forma idempotente:
   - `INSERT` / `UPDATE` → `save()` (UPSERT pela PK)
   - `DELETE` → `deleteById()` apenas se o registro existir
3. Em caso de falha, o **`DlqProducer`** envia a mensagem para `tucc-sync-event.dlq`.
4. O schema do PostgreSQL é criado e versionado via **Flyway**.

## Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Endereço do broker Kafka |
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuário do PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Senha do PostgreSQL |

## Build e execução

```bash
./mvnw clean package
./mvnw spring-boot:run
```

## Tópicos Kafka consumidos / produzidos

| Tópico | Papel |
|--------|-------|
| `tucc-sync-event` | Consumido — eventos CDC do producer |
| `tucc-sync-event.dlq` | Produzido — mensagens com falha de processamento |
