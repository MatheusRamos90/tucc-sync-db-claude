package br.com.matheushramos.tucc_core.service;

import br.com.matheushramos.tucc_core.dto.ProdutoEmpresaResponse;
import br.com.matheushramos.tucc_core.exception.ResourceNotFoundException;
import br.com.matheushramos.tucc_core.repository.ProdutoEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoEmpresaService {

    private final ProdutoEmpresaRepository produtoEmpresaRepository;

    @Transactional(readOnly = true)
    public Page<ProdutoEmpresaResponse> listar(Pageable pageable) {
        return produtoEmpresaRepository.findAll(pageable).map(ProdutoEmpresaResponse::from);
    }

    @Transactional(readOnly = true)
    public ProdutoEmpresaResponse buscar(Long id) {
        return produtoEmpresaRepository.findById(id)
                .map(ProdutoEmpresaResponse::from)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Associação produto-empresa não encontrada: " + id));
    }
}
