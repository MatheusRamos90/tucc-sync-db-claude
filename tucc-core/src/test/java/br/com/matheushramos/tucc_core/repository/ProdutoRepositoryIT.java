package br.com.matheushramos.tucc_core.repository;

import br.com.matheushramos.tucc_core.entity.ProdutoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=",
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

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO produto (id, nome, valor) VALUES (?, ?, ?)",
                1L, "Produto A", new BigDecimal("99.99"));
        jdbcTemplate.update(
                "INSERT INTO produto (id, nome, valor, desconto) VALUES (?, ?, ?, ?)",
                2L, "Produto B", new BigDecimal("49.90"), new BigDecimal("5.00"));
    }

    @Test
    void findById_quandoExiste_deveRetornarEntidadeComCamposCorretos() {
        Optional<ProdutoEntity> found = produtoRepository.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Produto A");
        assertThat(found.get().getValor()).isEqualByComparingTo("99.99");
    }

    @Test
    void findById_quandoNaoExiste_deveRetornarEmpty() {
        assertThat(produtoRepository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_comPaginacao_deveRetornarPage() {
        Page<ProdutoEntity> page = produtoRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
