package br.com.matheushramos.tucc_core.service;

import br.com.matheushramos.tucc_core.dto.ProdutoEmpresaResponse;
import br.com.matheushramos.tucc_core.entity.EmpresaEntity;
import br.com.matheushramos.tucc_core.entity.ProdutoEmpresaEntity;
import br.com.matheushramos.tucc_core.entity.ProdutoEntity;
import br.com.matheushramos.tucc_core.exception.ResourceNotFoundException;
import br.com.matheushramos.tucc_core.repository.ProdutoEmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoEmpresaServiceTest {

    @Mock ProdutoEmpresaRepository produtoEmpresaRepository;
    @InjectMocks ProdutoEmpresaService produtoEmpresaService;

    @Test
    void listar_deveChamarRepositoryERetornarPageMapeado() {
        ProdutoEmpresaEntity entity = buildEntity(100L, 1L, 10L);
        when(produtoEmpresaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = produtoEmpresaService.listar(PageRequest.of(0, 20));

        verify(produtoEmpresaRepository).findAll(any(Pageable.class));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isInstanceOf(ProdutoEmpresaResponse.class);
        assertThat(result.getContent().get(0).produtoId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).empresaId()).isEqualTo(10L);
    }

    @Test
    void buscar_quandoEncontrado_deveRetornarDTO() {
        ProdutoEmpresaEntity entity = buildEntity(100L, 1L, 10L);
        when(produtoEmpresaRepository.findById(100L)).thenReturn(Optional.of(entity));

        ProdutoEmpresaResponse result = produtoEmpresaService.buscar(100L);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.produtoNome()).isEqualTo("Produto A");
        assertThat(result.empresaNome()).isEqualTo("Empresa X");
    }

    @Test
    void buscar_quandoNaoEncontrado_deveLancarResourceNotFoundException() {
        when(produtoEmpresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> produtoEmpresaService.buscar(99L));
    }

    private ProdutoEmpresaEntity buildEntity(Long id, Long produtoId, Long empresaId) {
        ProdutoEntity produto = mock(ProdutoEntity.class);
        when(produto.getNome()).thenReturn("Produto A");

        EmpresaEntity empresa = mock(EmpresaEntity.class);
        when(empresa.getNome()).thenReturn("Empresa X");

        ProdutoEmpresaEntity entity = mock(ProdutoEmpresaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getProdutoId()).thenReturn(produtoId);
        when(entity.getEmpresaId()).thenReturn(empresaId);
        when(entity.getProduto()).thenReturn(produto);
        when(entity.getEmpresa()).thenReturn(empresa);
        return entity;
    }
}
