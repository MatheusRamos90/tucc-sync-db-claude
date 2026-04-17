package br.com.matheushramos.tucc_sync_db_consumer.repository;

import br.com.matheushramos.tucc_sync_db_consumer.entity.EmpresaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
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

    @Test
    void save_devePersistirTodosOsCampos() {
        EmpresaEntity entity = new EmpresaEntity();
        entity.setId(10L);
        entity.setNome("Empresa X");
        entity.setCnpj("12345678000199");
        empresaRepository.save(entity);

        Optional<EmpresaEntity> found = empresaRepository.findById(10L);
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Empresa X");
        assertThat(found.get().getCnpj()).isEqualTo("12345678000199");
    }

    @Test
    void existsById_quandoExiste_deveRetornarTrue() {
        EmpresaEntity entity = new EmpresaEntity();
        entity.setId(20L);
        entity.setNome("Empresa Y");
        entity.setCnpj("98765432000188");
        empresaRepository.save(entity);

        assertThat(empresaRepository.existsById(20L)).isTrue();
        assertThat(empresaRepository.existsById(999L)).isFalse();
    }

    @Test
    void deleteById_deveRemoverEntidade() {
        EmpresaEntity entity = new EmpresaEntity();
        entity.setId(30L);
        entity.setNome("Empresa Z");
        entity.setCnpj("11223344000155");
        empresaRepository.save(entity);

        empresaRepository.deleteById(30L);

        assertThat(empresaRepository.findById(30L)).isEmpty();
    }
}
