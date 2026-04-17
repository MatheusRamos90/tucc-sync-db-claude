---
name: kafka-consumer
description: Guia para implementar ou modificar o tucc-sync-db-consumer. Use quando trabalhar com consumo de mensagens Kafka, sincronização com PostgreSQL, idempotência, DLQ e retry.
allowed-tools: Read, Grep, Glob, Edit, Bash
---

# Skill: tucc-sync-db-consumer

## Responsabilidade
Consumir mensagens do tópico `tucc-sync-event` e persistir as mudanças no PostgreSQL de forma idempotente.

## Fluxo de Processamento

```
tucc-sync-event
      │
      ▼
  Deserializar SyncEventPayload
      │
      ▼
  Identificar tabela + operação
      │
      ├── INSERT/UPDATE → upsert() com verificação de existência
      ├── DELETE        → deleteIfExists() — sem erro se não encontrado
      └── Desconhecido  → log.warn() + ignorar (não vai para DLQ)
      │
      ├── [Sucesso] → commit offset
      └── [Falha]   → encaminhar para tucc-sync-event.dlq + log detalhado
```

## Regras de Idempotência

- **INSERT/UPDATE**: use `UPSERT` no Postgres (INSERT ... ON CONFLICT DO UPDATE)
- **DELETE**: use `deleteById()` apenas se existir — nunca lance exceção por registro ausente
- O ID do Oracle é a chave natural no Postgres — nunca gere IDs internos

## DLQ e Retry

```
tucc-sync-event.dlq   → mensagens com falha de processamento
tucc-sync-event.retry → reenvio manual/automático para reprocessamento
```

Ao encaminhar para DLQ, inclua:
- A mensagem original
- O stacktrace da exceção
- Timestamp da falha

## Checklist de Implementação

- [ ] `@KafkaListener` com `groupId` definido via config
- [ ] Commit manual de offset (`AckMode.MANUAL_IMMEDIATE`)
- [ ] Idempotência garantida para INSERT, UPDATE e DELETE
- [ ] Encaminhamento para DLQ em qualquer exceção não tratada
- [ ] `@Transactional` no método de persistência (não no listener)
- [ ] Log de cada mensagem processada com resultado (SUCCESS/DLQ)
- [ ] Testes unitários: cenários de INSERT, UPDATE, DELETE e falha → DLQ

## Mapeamento de Entidades

| Mensagem (table)   | Entidade JPA     | Repositório              |
|--------------------|------------------|--------------------------|
| PRODUTO            | ProdutoEntity    | ProdutoRepository        |
| EMPRESA            | EmpresaEntity    | EmpresaRepository        |
| PRODUTO_EMPRESA    | ProdEmpresaEntity| ProdEmpresaRepository    |

## Referência de Código

```java
@KafkaListener(topics = "${kafka.topics.sync-event}", groupId = "${kafka.consumer.group-id}")
public void consume(SyncEventPayload payload, Acknowledgment ack) {
    try {
        syncService.process(payload);
        ack.acknowledge();
        log.info("Processado: table={} op={}", payload.table(), payload.operation());
    } catch (Exception ex) {
        dlqProducer.send(payload, ex);
        ack.acknowledge(); // Sempre commite para não travar a fila
        log.error("Erro ao processar, enviado para DLQ: {}", payload, ex);
    }
}
```

## Testes

### Unitário — `SyncServiceTest`, `SyncEventConsumerTest`
`@ExtendWith(MockitoExtension.class)` + `@Mock` para repositórios/DlqProducer. Não precisa de contexto Spring.

### Componente — `SyncEventConsumerComponentTest`
Testa a cadeia `SyncEventConsumer → SyncService → Repository (mock)`. Usa H2 para fornecer o `JpaTransactionManager` necessário ao `@Transactional` do `SyncService` sem PostgreSQL real.

```java
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
    "spring.datasource.url=jdbc:h2:mem:testconsumerdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.default_schema="
})
class SyncEventConsumerComponentTest {

    @Autowired SyncEventConsumer syncEventConsumer;
    @MockitoBean ProdutoRepository produtoRepository;
    @MockitoBean EmpresaRepository empresaRepository;
    @MockitoBean ProdutoEmpresaRepository produtoEmpresaRepository;
    @MockitoBean DlqProducer dlqProducer;  // isola infraestrutura Kafka

    @Test
    void consume_produtoInsert_chamaSaveEAcknowledge() {
        SyncEventPayload payload = new SyncEventPayload("PRODUTO", "INSERT", null, Map.of(),
            Map.of("ID", 1, "NOME", "Produto A", "VALOR", "99.99"));
        Acknowledgment ack = mock(Acknowledgment.class);

        syncEventConsumer.consume(payload, ack);

        verify(produtoRepository).save(any(ProdutoEntity.class));
        verify(ack).acknowledge();
    }
    // Também testar: DELETE (existsById=true), exceção → DLQ chamado + ack chamado
}
```

**Pontos-chave:**
- `SyncEventConsumer.consume()` é invocado diretamente (sem Kafka broker)
- H2 + `ddl-auto=validate` + Flyway → schema criado pelas migrations V1/V2/V3 no H2
- Kafka excluído via `spring.autoconfigure.exclude` (apenas `KafkaAutoConfiguration`)
- `DlqProducer` mockado para isolar a camada de infraestrutura Kafka
- `ack.acknowledge()` sempre chamado: verificar no caminho feliz E no de erro

### Integração (Testcontainers) — `*RepositoryIT`
Verifica que o mapeamento JPA está em conformidade com o schema real do PostgreSQL.

> **Spring Boot 4:** `@DataJpaTest`, `@AutoConfigureTestDatabase` e `@ImportAutoConfiguration` foram removidos. Use `@SpringBootTest(webEnvironment=NONE)` + `@Transactional`. Flyway autoconfiguration também não existe — use `ddl-auto=create-drop`.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Transactional
class ProdutoRepositoryIT {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired ProdutoRepository produtoRepository;

    @Test
    void save_devePersistirTodosOsCampos() {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(1L);
        entity.setNome("Produto A");
        entity.setValor(new BigDecimal("99.99"));
        produtoRepository.save(entity);

        Optional<ProdutoEntity> found = produtoRepository.findById(1L);
        assertThat(found.get().getNome()).isEqualTo("Produto A");
    }
}
```

**Pontos-chave:**
- `@Transactional` no teste garante rollback automático após cada método → isolamento total
- `ddl-auto=create-drop`: Hibernate cria schema a partir das entidades (sem Flyway)
- `spring.autoconfigure.exclude` sobrescreve o test yaml (que excluiria DataSource/JPA)
- Para `ProdutoEmpresaRepositoryIT`: salvar Empresa + Produto no `@BeforeEach` (constraints FK)
- Surefire deve incluir `**/*IT.java` nas includes — configurar via `maven-surefire-plugin`
