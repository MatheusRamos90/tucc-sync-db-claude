package br.com.matheushramos.tucc_sync_db_producer.debezium;

import br.com.matheushramos.tucc_sync_db_producer.payload.SyncEventPayload;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class DebeziumEventHandlerComponentTest {

    @MockitoBean
    KafkaTemplate<String, SyncEventPayload> kafkaTemplate;

    @Autowired
    DebeziumEventHandler debeziumEventHandler;

    @Test
    void handleBatch_produtoInsert_publicaNoTopicSyncEvent() throws InterruptedException {
        ChangeEvent<String, String> event = mockChangeEvent(
                "tucc.TUCC.PRODUTO",
                """
                {"op":"c","source":{"ts_ms":1703001600000},
                 "before":null,
                 "after":{"ID":1,"NOME":"Produto A","VALOR":"99.99","DESCONTO":null,
                           "DT_CRIACAO":null,"DT_ATUALIZACAO":null}}
                """);
        DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer = mock(DebeziumEngine.RecordCommitter.class);

        debeziumEventHandler.handleBatch(List.of(event), committer);

        verify(kafkaTemplate).send(eq("tucc-sync-event"), eq("PRODUTO"), any(SyncEventPayload.class));
        verify(committer).markProcessed(event);
        verify(committer).markBatchFinished();
    }

    @Test
    void handleBatch_empresaDelete_publicaComOperacaoDelete() throws InterruptedException {
        ChangeEvent<String, String> event = mockChangeEvent(
                "tucc.TUCC.EMPRESA",
                """
                {"op":"d","source":{"ts_ms":1703001600000},
                 "before":{"ID":10,"NOME":"Empresa X","CNPJ":"12345678000199",
                            "DT_CRIACAO":null,"DT_ATUALIZACAO":null},
                 "after":null}
                """);
        DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer = mock(DebeziumEngine.RecordCommitter.class);

        debeziumEventHandler.handleBatch(List.of(event), committer);

        verify(kafkaTemplate).send(eq("tucc-sync-event"), eq("EMPRESA"), any(SyncEventPayload.class));
        verify(committer).markProcessed(event);
        verify(committer).markBatchFinished();
    }

    @Test
    void handleBatch_tombstone_naoPublicaNoKafka() throws InterruptedException {
        ChangeEvent<String, String> event = mockChangeEvent("tucc.TUCC.PRODUTO", null);
        DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer = mock(DebeziumEngine.RecordCommitter.class);

        debeziumEventHandler.handleBatch(List.of(event), committer);

        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(committer).markProcessed(event);
        verify(committer).markBatchFinished();
    }

    @SuppressWarnings("unchecked")
    private ChangeEvent<String, String> mockChangeEvent(String destination, String value) {
        ChangeEvent<String, String> event = mock(ChangeEvent.class);
        when(event.destination()).thenReturn(destination);
        when(event.value()).thenReturn(value);
        return event;
    }
}
