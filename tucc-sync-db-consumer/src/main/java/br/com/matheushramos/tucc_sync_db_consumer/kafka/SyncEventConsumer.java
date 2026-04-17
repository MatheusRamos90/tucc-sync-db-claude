package br.com.matheushramos.tucc_sync_db_consumer.kafka;

import br.com.matheushramos.tucc_sync_db_consumer.payload.SyncEventPayload;
import br.com.matheushramos.tucc_sync_db_consumer.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncEventConsumer {

    private final SyncService syncService;
    private final DlqProducer dlqProducer;

    @KafkaListener(
            topics = "${kafka.topics.sync-event}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(SyncEventPayload payload, Acknowledgment ack) {
        try {
            syncService.process(payload);
            log.info("SUCCESS table={} op={}", payload.table(), payload.operation());
        } catch (Exception ex) {
            try {
                dlqProducer.send(payload, ex);
                log.error("FAILED → DLQ table={} op={}", payload.table(), payload.operation(), ex);
            } catch (Exception dlqEx) {
                log.error("FAILED → Erro ao enviar para DLQ table={} op={}",
                        payload.table(), payload.operation(), dlqEx);
            }
        } finally {
            ack.acknowledge();
        }
    }
}
