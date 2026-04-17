package br.com.matheushramos.tucc_sync_db_consumer.repository;

import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Transactional
class ProdutoRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    ProdutoRepository produtoRepository;

    @Test
    void save_devePersistirTodosOsCampos() {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(1L);
        entity.setNome("Produto A");
        entity.setValor(new BigDecimal("99.99"));
        entity.setDesconto(new BigDecimal("5.00"));
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        produtoRepository.save(entity);

        Optional<ProdutoEntity> found = produtoRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Produto A");
        assertThat(found.get().getValor()).isEqualByComparingTo("99.99");
        assertThat(found.get().getDesconto()).isEqualByComparingTo("5.00");
        assertThat(found.get().getCreatedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void existsById_quandoExiste_deveRetornarTrue() {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(2L);
        entity.setNome("Produto B");
        entity.setValor(new BigDecimal("49.90"));
        produtoRepository.save(entity);

        assertThat(produtoRepository.existsById(2L)).isTrue();
        assertThat(produtoRepository.existsById(999L)).isFalse();
    }

    @Test
    void deleteById_deveRemoverEntidade() {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(3L);
        entity.setNome("Produto C");
        entity.setValor(new BigDecimal("19.90"));
        produtoRepository.save(entity);

        produtoRepository.deleteById(3L);

        assertThat(produtoRepository.findById(3L)).isEmpty();
    }
}
