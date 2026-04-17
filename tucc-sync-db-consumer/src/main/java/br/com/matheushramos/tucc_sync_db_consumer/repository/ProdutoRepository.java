package br.com.matheushramos.tucc_sync_db_consumer.repository;

import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {
}
