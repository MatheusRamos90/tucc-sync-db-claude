package br.com.matheushramos.tucc_sync_db_consumer.repository;

import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoEmpresaRepository extends JpaRepository<ProdutoEmpresaEntity, Long> {
}
