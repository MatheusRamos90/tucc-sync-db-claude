package br.com.matheushramos.tucc_core.repository;

import br.com.matheushramos.tucc_core.entity.ProdutoEmpresaEntity;
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
class ProdutoEmpresaRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    ProdutoEmpresaRepository produtoEmpresaRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO produto (id, nome, valor) VALUES (?, ?, ?)",
                1L, "Produto A", new BigDecimal("99.99"));
        jdbcTemplate.update(
                "INSERT INTO empresa (id, nome, cnpj) VALUES (?, ?, ?)",
                10L, "Empresa X", "12345678000199");
        jdbcTemplate.update(
                "INSERT INTO produto_empresa (id, produto_id, empresa_id) VALUES (?, ?, ?)",
                100L, 1L, 10L);
    }

    @Test
    void findById_quandoExiste_deveRetornarEntidadeComFKsCorretos() {
        Optional<ProdutoEmpresaEntity> found = produtoEmpresaRepository.findById(100L);

        assertThat(found).isPresent();
        assertThat(found.get().getProdutoId()).isEqualTo(1L);
        assertThat(found.get().getEmpresaId()).isEqualTo(10L);
    }

    @Test
    void findById_quandoExiste_devePermitirNavegacaoLazy() {
        Optional<ProdutoEmpresaEntity> found = produtoEmpresaRepository.findById(100L);

        assertThat(found).isPresent();
        assertThat(found.get().getProduto().getNome()).isEqualTo("Produto A");
        assertThat(found.get().getEmpresa().getNome()).isEqualTo("Empresa X");
    }

    @Test
    void findById_quandoNaoExiste_deveRetornarEmpty() {
        assertThat(produtoEmpresaRepository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_comPaginacao_deveRetornarPage() {
        Page<ProdutoEmpresaEntity> page = produtoEmpresaRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
