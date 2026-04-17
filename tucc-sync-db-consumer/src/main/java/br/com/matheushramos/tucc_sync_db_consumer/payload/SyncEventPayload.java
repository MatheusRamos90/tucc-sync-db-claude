package br.com.matheushramos.tucc_sync_db_consumer.payload;

import java.time.Instant;
import java.util.Map;

public record SyncEventPayload(
    String table,
    String operation,
    Instant capturedAt,
    Map<String, Object> before,
    Map<String, Object> after
) {}
