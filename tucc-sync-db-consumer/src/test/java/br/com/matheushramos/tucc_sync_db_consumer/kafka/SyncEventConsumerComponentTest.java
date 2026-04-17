package br.com.matheushramos.tucc_sync_db_consumer.kafka;

import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEntity;
import br.com.matheushramos.tucc_sync_db_consumer.payload.SyncEventPayload;
import br.com.matheushramos.tucc_sync_db_consumer.repository.EmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoEmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Teste de componente: passa por todas as camadas (Consumer → SyncService) com
 * repositories mockados. Usa H2 para fornecer o JpaTransactionManager necessário
 * ao @Transactional do SyncService sem necessidade de PostgreSQL real.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "spring.datasource.url=jdbc:h2:mem:testconsumerdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.default_schema="
})
class SyncEventConsumerComponentTest {

    @Autowired
    SyncEventConsumer syncEventConsumer;

    @MockitoBean
    ProdutoRepository produtoRepository;

    @MockitoBean
    EmpresaRepository empresaRepository;

    @MockitoBean
    ProdutoEmpresaRepository produtoEmpresaRepository;

    @MockitoBean
    DlqProducer dlqProducer;

    @Test
    void consume_produtoInsert_chamaSaveEAcknowledge() {
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "INSERT", null,
                Map.of(),
                Map.of("ID", 1, "NOME", "Produto A", "VALOR", "99.99"));
        Acknowledgment ack = mock(Acknowledgment.class);

        syncEventConsumer.consume(payload, ack);

        verify(produtoRepository).save(any(ProdutoEntity.class));
        verify(ack).acknowledge();
    }

    @Test
    void consume_empresaDelete_chamaDeleteByIdEAcknowledge() {
        when(empresaRepository.existsById(10L)).thenReturn(true);
        SyncEventPayload payload = new SyncEventPayload(
                "EMPRESA", "DELETE", null,
                Map.of("ID", 10),
                Map.of());
        Acknowledgment ack = mock(Acknowledgment.class);

        syncEventConsumer.consume(payload, ack);

        verify(empresaRepository).deleteById(10L);
        verify(ack).acknowledge();
    }

    @Test
    void consume_syncServiceLancaExcecao_enviaDlqEAckAindaEChamado() {
        doThrow(new RuntimeException("Erro de persistência"))
                .when(produtoRepository).save(any());
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "INSERT", null,
                Map.of(),
                Map.of("ID", 1, "NOME", "Produto A", "VALOR", "99.99"));
        Acknowledgment ack = mock(Acknowledgment.class);

        assertThatCode(() -> syncEventConsumer.consume(payload, ack)).doesNotThrowAnyException();

        verify(dlqProducer).send(any(SyncEventPayload.class), any(RuntimeException.class));
        verify(ack).acknowledge();
    }
}
