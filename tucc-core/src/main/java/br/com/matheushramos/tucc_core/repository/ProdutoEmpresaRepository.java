package br.com.matheushramos.tucc_core.repository;

import br.com.matheushramos.tucc_core.entity.ProdutoEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoEmpresaRepository extends JpaRepository<ProdutoEmpresaEntity, Long> {
}
