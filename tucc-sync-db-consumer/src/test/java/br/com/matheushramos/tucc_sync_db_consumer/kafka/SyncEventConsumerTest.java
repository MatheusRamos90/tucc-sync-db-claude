package br.com.matheushramos.tucc_sync_db_consumer.kafka;

import br.com.matheushramos.tucc_sync_db_consumer.payload.SyncEventPayload;
import br.com.matheushramos.tucc_sync_db_consumer.service.SyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncEventConsumerTest {

    @Mock SyncService syncService;
    @Mock DlqProducer dlqProducer;
    @Mock Acknowledgment ack;

    @InjectMocks SyncEventConsumer consumer;

    private final SyncEventPayload payload = new SyncEventPayload(
            "PRODUTO", "INSERT", Instant.now(), Map.of(),
            Map.of("ID", 1, "NOME", "X"));

    @Test
    void happyPath_deveChamarProcessEAck() {
        consumer.consume(payload, ack);

        verify(syncService).process(payload);
        verify(ack).acknowledge();
        verifyNoInteractions(dlqProducer);
    }

    @Test
    void quandoSyncServiceLancaExcecao_deveEnviarParaDLQEAindaAck() {
        doThrow(new RuntimeException("Erro de teste")).when(syncService).process(any());

        consumer.consume(payload, ack);

        verify(dlqProducer).send(eq(payload), any(RuntimeException.class));
        verify(ack).acknowledge();
    }

    @Test
    void offsetDeveSerSempreCommitado_mesmoQuandoDlqLancaExcecao() {
        doThrow(new RuntimeException("Erro sync")).when(syncService).process(any());
        doThrow(new RuntimeException("Erro DLQ")).when(dlqProducer).send(any(), any());

        try {
            consumer.consume(payload, ack);
        } catch (Exception ignored) {
            // esperado no caso de DLQ também falhar
        }

        verify(ack).acknowledge();
    }
}
