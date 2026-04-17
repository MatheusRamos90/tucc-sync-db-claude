package br.com.matheushramos.tucc_sync_db_consumer.repository;

import br.com.matheushramos.tucc_sync_db_consumer.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long> {
}
