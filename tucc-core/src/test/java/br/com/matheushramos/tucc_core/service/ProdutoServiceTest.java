package br.com.matheushramos.tucc_core.service;

import br.com.matheushramos.tucc_core.dto.ProdutoResponse;
import br.com.matheushramos.tucc_core.entity.ProdutoEntity;
import br.com.matheushramos.tucc_core.exception.ResourceNotFoundException;
import br.com.matheushramos.tucc_core.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock ProdutoRepository produtoRepository;
    @InjectMocks ProdutoService produtoService;

    @Test
    void listar_deveChamarRepositoryERetornarPageMapeado() {
        ProdutoEntity entity = buildEntity(1L, "Produto A");
        when(produtoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = produtoService.listar(PageRequest.of(0, 20));

        verify(produtoRepository).findAll(any(Pageable.class));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Produto A");
        assertThat(result.getContent().get(0)).isInstanceOf(ProdutoResponse.class);
    }

    @Test
    void buscar_quandoEncontrado_deveRetornarDTO() {
        ProdutoEntity entity = buildEntity(1L, "Produto A");
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(entity));

        ProdutoResponse result = produtoService.buscar(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nome()).isEqualTo("Produto A");
        assertThat(result.valor()).isEqualByComparingTo("99.99");
    }

    @Test
    void buscar_quandoNaoEncontrado_deveLancarResourceNotFoundException() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> produtoService.buscar(99L));
    }

    private ProdutoEntity buildEntity(Long id, String nome) {
        // Usa reflexão via Mockito, mas para testes de service usamos spy ou construtor
        // Como ProdutoEntity usa @Getter sem setter (read-only), usamos mock
        ProdutoEntity entity = org.mockito.Mockito.mock(ProdutoEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getNome()).thenReturn(nome);
        when(entity.getValor()).thenReturn(new BigDecimal("99.99"));
        when(entity.getDesconto()).thenReturn(new BigDecimal("5.00"));
        return entity;
    }
}
