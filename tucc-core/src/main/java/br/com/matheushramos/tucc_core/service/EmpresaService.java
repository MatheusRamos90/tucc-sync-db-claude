package br.com.matheushramos.tucc_core.service;

import br.com.matheushramos.tucc_core.dto.EmpresaResponse;
import br.com.matheushramos.tucc_core.exception.ResourceNotFoundException;
import br.com.matheushramos.tucc_core.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(Pageable pageable) {
        return empresaRepository.findAll(pageable).map(EmpresaResponse::from);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscar(Long id) {
        return empresaRepository.findById(id)
                .map(EmpresaResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
    }
}
