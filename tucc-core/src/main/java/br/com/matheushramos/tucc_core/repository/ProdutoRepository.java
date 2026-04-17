package br.com.matheushramos.tucc_core.repository;

import br.com.matheushramos.tucc_core.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {
}
