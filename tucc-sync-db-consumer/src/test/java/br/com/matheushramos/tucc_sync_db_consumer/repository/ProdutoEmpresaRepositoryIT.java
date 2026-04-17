package br.com.matheushramos.tucc_sync_db_consumer.repository;

import br.com.matheushramos.tucc_sync_db_consumer.entity.EmpresaEntity;
import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEmpresaEntity;
import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
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
    ProdutoRepository produtoRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @BeforeEach
    void setUp() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Produto A");
        produto.setValor(new BigDecimal("99.99"));
        produtoRepository.save(produto);

        EmpresaEntity empresa = new EmpresaEntity();
        empresa.setId(10L);
        empresa.setNome("Empresa X");
        empresa.setCnpj("12345678000199");
        empresaRepository.save(empresa);
    }

    @Test
    void save_devePersistirComFKsCorretas() {
        ProdutoEmpresaEntity entity = new ProdutoEmpresaEntity();
        entity.setId(100L);
        entity.setProdutoId(1L);
        entity.setEmpresaId(10L);
        produtoEmpresaRepository.save(entity);

        Optional<ProdutoEmpresaEntity> found = produtoEmpresaRepository.findById(100L);
        assertThat(found).isPresent();
        assertThat(found.get().getProdutoId()).isEqualTo(1L);
        assertThat(found.get().getEmpresaId()).isEqualTo(10L);
    }

    @Test
    void existsById_quandoExiste_deveRetornarTrue() {
        ProdutoEmpresaEntity entity = new ProdutoEmpresaEntity();
        entity.setId(200L);
        entity.setProdutoId(1L);
        entity.setEmpresaId(10L);
        produtoEmpresaRepository.save(entity);

        assertThat(produtoEmpresaRepository.existsById(200L)).isTrue();
        assertThat(produtoEmpresaRepository.existsById(999L)).isFalse();
    }

    @Test
    void deleteById_deveRemoverEntidade() {
        ProdutoEmpresaEntity entity = new ProdutoEmpresaEntity();
        entity.setId(300L);
        entity.setProdutoId(1L);
        entity.setEmpresaId(10L);
        produtoEmpresaRepository.save(entity);

        produtoEmpresaRepository.deleteById(300L);

        assertThat(produtoEmpresaRepository.findById(300L)).isEmpty();
    }
}
