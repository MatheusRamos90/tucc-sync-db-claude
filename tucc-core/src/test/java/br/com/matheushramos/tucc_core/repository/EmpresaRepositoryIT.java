package br.com.matheushramos.tucc_core.repository;

import br.com.matheushramos.tucc_core.entity.EmpresaEntity;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Transactional
class EmpresaRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO empresa (id, nome, cnpj) VALUES (?, ?, ?)",
                10L, "Empresa X", "12345678000199");
        jdbcTemplate.update(
                "INSERT INTO empresa (id, nome, cnpj) VALUES (?, ?, ?)",
                20L, "Empresa Y", "98765432000188");
    }

    @Test
    void findById_quandoExiste_deveRetornarEntidadeComCamposCorretos() {
        Optional<EmpresaEntity> found = empresaRepository.findById(10L);

        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Empresa X");
        assertThat(found.get().getCnpj()).isEqualTo("12345678000199");
    }

    @Test
    void findById_quandoNaoExiste_deveRetornarEmpty() {
        assertThat(empresaRepository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_comPaginacao_deveRetornarPage() {
        Page<EmpresaEntity> page = empresaRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
