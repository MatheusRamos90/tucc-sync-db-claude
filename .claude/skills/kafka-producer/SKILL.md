---
name: kafka-producer
description: Guia para implementar ou modificar o tucc-sync-db-producer. Use quando trabalhar com captura CDC do Oracle via Debezium, mapeamento de eventos, serialização e publicação no Kafka.
allowed-tools: Read, Grep, Glob, Edit, Bash
---

# Skill: tucc-sync-db-producer

## Responsabilidade
Capturar mudanças do Oracle via Debezium e publicar mensagens estruturadas no tópico Kafka `tucc-sync-event`.

## Fluxo de Implementação

1. **Configurar conector Debezium** para monitorar as tabelas: PRODUTO, EMPRESA, PRODUTO_EMPRESA
2. **Mapear evento Debezium** → `SyncEventPayload` (record Java 21)
3. **Serializar em JSON** e publicar no tópico Kafka
4. **Tratar falhas** de publicação (retry com backoff exponencial)

## Estrutura do Payload

```json
{
  "table": "PRODUTO",
  "operation": "UPDATE",
  "capturedAt": "2025-04-15T10:00:00Z",
  "before": { "ID": 1, "NOME": "Produto A", "VALOR": 100.00 },
  "after":  { "ID": 1, "NOME": "Produto A Atualizado", "VALOR": 120.00 }
}
```

## Tabelas Monitoradas
- `PRODUTO` — campos: ID, NOME, VALOR, DESCONTO, DT_CRIACAO, DT_ATUALIZACAO
- `EMPRESA` — campos: ID, NOME, CNPJ, DT_CRIACAO, DT_ATUALIZACAO
- `PRODUTO_EMPRESA` — campos: ID, PRODUTO_ID, EMPRESA_ID

## Checklist de Implementação

- [ ] `SyncEventPayload` como record imutável
- [ ] Configuração Debezium via `application.yml` (não hardcoded)
- [ ] Tópico definido como constante: `KafkaTopics.SYNC_EVENT`
- [ ] Log de cada mensagem publicada (table + operation + offset)
- [ ] Retry configurado para falhas de publicação (máx. 3 tentativas)
- [ ] Teste unitário do mapeador Debezium → SyncEventPayload

## Referências de Código

```java
public record SyncEventPayload(
    String table,
    String operation,
    Instant capturedAt,
    Map<String, Object> before,
    Map<String, Object> after
) {}
```

## Testes

### Unitário — `DebeziumEventMapperTest`
`@ExtendWith(MockitoExtension.class)` — instancia `DebeziumEventMapper(new ObjectMapper())` diretamente e testa o mapeamento de JSON bruto → `Optional<SyncEventPayload>` para todos os `op` codes e tabelas.

### Componente — `DebeziumEventHandlerComponentTest`
Testa a cadeia completa `DebeziumEventHandler → DebeziumEventMapper → KafkaTemplate (mock)`.

```java
@SpringBootTest   // DebeziumConfig/EngineRunner não carregam (test yaml: debezium.enabled=false)
class DebeziumEventHandlerComponentTest {

    @MockitoBean
    KafkaTemplate<String, SyncEventPayload> kafkaTemplate;

    @Autowired
    DebeziumEventHandler debeziumEventHandler;

    @Test
    void handleBatch_produtoInsert_publicaNoTopicSyncEvent() throws InterruptedException {
        ChangeEvent<String, String> event = mockChangeEvent(
            "tucc.TUCC.PRODUTO",
            "{\"op\":\"c\",\"source\":{\"ts_ms\":1703001600000},\"after\":{...}}");
        RecordCommitter<ChangeEvent<String, String>> committer = mock(RecordCommitter.class);

        debeziumEventHandler.handleBatch(List.of(event), committer);

        verify(kafkaTemplate).send(eq("tucc-sync-event"), eq("PRODUTO"), any(SyncEventPayload.class));
        verify(committer).markProcessed(event);
        verify(committer).markBatchFinished();
    }

    // Também testar: DELETE, tombstone (value=null → kafkaTemplate.send() NÃO chamado)
}
```

**Pontos-chave:**
- `DebeziumConfig` e `DebeziumEngineRunner` têm `@ConditionalOnProperty(debezium.enabled)` → não carregam nos testes
- `ChangeEvent` e `RecordCommitter` são interfaces Debezium → criar via `mock()`
- Topic name e table key verificados com `eq()`
