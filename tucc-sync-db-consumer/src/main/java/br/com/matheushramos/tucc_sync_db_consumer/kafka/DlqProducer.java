package br.com.matheushramos.tucc_sync_db_consumer.kafka;

import br.com.matheushramos.tucc_sync_db_consumer.payload.SyncEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.dlq}")
    private String dlqTopic;

    public void send(SyncEventPayload originalPayload, Exception cause) {
        Map<String, Object> dlqMessage = new LinkedHashMap<>();
        dlqMessage.put("originalPayload", originalPayload);
        dlqMessage.put("errorMessage", cause.getMessage());
        dlqMessage.put("stackTrace", Arrays.toString(cause.getStackTrace()));
        dlqMessage.put("failedAt", Instant.now().toString());

        kafkaTemplate.send(dlqTopic, dlqMessage);
        log.error("Payload enviado para DLQ. table={} op={}",
                originalPayload.table(), originalPayload.operation(), cause);
    }
}
