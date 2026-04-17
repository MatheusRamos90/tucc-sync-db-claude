package br.com.matheushramos.tucc_sync_db_producer.debezium;

import br.com.matheushramos.tucc_sync_db_producer.payload.SyncEventPayload;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumEventHandler
        implements DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {

    private final DebeziumEventMapper mapper;
    private final KafkaTemplate<String, SyncEventPayload> kafkaTemplate;

    @Value("${kafka.topics.sync-event}")
    private String syncEventTopic;

    @Override
    public void handleBatch(
            List<ChangeEvent<String, String>> records,
            DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer)
            throws InterruptedException {

        for (ChangeEvent<String, String> event : records) {
            try {
                mapper.map(event.destination(), event.value()).ifPresentOrElse(
                    payload -> {
                        kafkaTemplate.send(syncEventTopic, payload.table(), payload);
                        log.info("Publicado: table={} op={} topic={}",
                                payload.table(), payload.operation(), syncEventTopic);
                    },
                    () -> log.debug("Evento ignorado pelo mapper. destination={}",
                            event.destination())
                );
            } catch (Exception e) {
                log.error("Falha ao publicar evento no Kafka, ignorando para não bloquear pipeline. " +
                        "destination={}", event.destination(), e);
            }
            committer.markProcessed(event);
        }
        committer.markBatchFinished();
    }
}
