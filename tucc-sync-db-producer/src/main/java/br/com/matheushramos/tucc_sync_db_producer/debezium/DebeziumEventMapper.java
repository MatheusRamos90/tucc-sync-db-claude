package br.com.matheushramos.tucc_sync_db_producer.debezium;

import br.com.matheushramos.tucc_sync_db_producer.payload.SyncEventPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumEventMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public Optional<SyncEventPayload> map(String topic, String valueJson) {
        if (valueJson == null) {
            return Optional.empty();
        }

        try {
            Map<String, Object> envelope = objectMapper.readValue(valueJson, MAP_TYPE);

            // Debezium embedded may emit schema+payload format: { "schema":{}, "payload":{...} }
            // or flat format: { "op":"c", "before":{}, "after":{} }
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = envelope.get("payload") instanceof Map<?, ?>
                    ? (Map<String, Object>) envelope.get("payload")
                    : envelope;

            String op = (String) payload.get("op");
            if (op == null) {
                log.warn("Evento sem campo 'op', ignorado. topic={}", topic);
                return Optional.empty();
            }

            String operation = switch (op) {
                case "c" -> "INSERT";
                case "u" -> "UPDATE";
                case "d" -> "DELETE";
                case "r" -> "INSERT";
                default  -> {
                    log.warn("Operação Debezium desconhecida: op={} topic={}", op, topic);
                    yield "UNKNOWN";
                }
            };

            String table = extractTableName(topic);
            Instant capturedAt = extractTimestamp(payload);

            @SuppressWarnings("unchecked")
            Map<String, Object> before = payload.get("before") instanceof Map<?,?>
                    ? (Map<String, Object>) payload.get("before")
                    : Map.of();

            @SuppressWarnings("unchecked")
            Map<String, Object> after = payload.get("after") instanceof Map<?,?>
                    ? (Map<String, Object>) payload.get("after")
                    : Map.of();

            return Optional.of(new SyncEventPayload(table, operation, capturedAt,
                    new LinkedHashMap<>(before), new LinkedHashMap<>(after)));

        } catch (Exception e) {
            log.error("Falha ao mapear evento Debezium. topic={}", topic, e);
            return Optional.empty();
        }
    }

    private String extractTableName(String topic) {
        // topic format: tucc.SCHEMA.TABLE_NAME
        String[] parts = topic.split("\\.");
        return parts[parts.length - 1];
    }

    @SuppressWarnings("unchecked")
    private Instant extractTimestamp(Map<String, Object> envelope) {
        try {
            Object source = envelope.get("source");
            if (source instanceof Map<?,?> sourceMap) {
                Object tsMs = ((Map<String, Object>) sourceMap).get("ts_ms");
                if (tsMs instanceof Number number) {
                    return Instant.ofEpochMilli(number.longValue());
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível extrair timestamp do evento, usando Instant.now()", e);
        }
        return Instant.now();
    }
}
