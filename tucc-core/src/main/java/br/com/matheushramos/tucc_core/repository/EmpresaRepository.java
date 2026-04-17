package br.com.matheushramos.tucc_core.repository;

import br.com.matheushramos.tucc_core.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long> {
}
