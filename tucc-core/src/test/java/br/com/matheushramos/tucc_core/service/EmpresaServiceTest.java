package br.com.matheushramos.tucc_core.service;

import br.com.matheushramos.tucc_core.dto.EmpresaResponse;
import br.com.matheushramos.tucc_core.entity.EmpresaEntity;
import br.com.matheushramos.tucc_core.exception.ResourceNotFoundException;
import br.com.matheushramos.tucc_core.repository.EmpresaRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock EmpresaRepository empresaRepository;
    @InjectMocks EmpresaService empresaService;

    @Test
    void listar_deveChamarRepositoryERetornarPageMapeado() {
        EmpresaEntity entity = buildEntity(10L, "Empresa X", "12345678000199");
        when(empresaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var result = empresaService.listar(PageRequest.of(0, 20));

        verify(empresaRepository).findAll(any(Pageable.class));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isInstanceOf(EmpresaResponse.class);
        assertThat(result.getContent().get(0).cnpj()).isEqualTo("12345678000199");
    }

    @Test
    void buscar_quandoEncontrado_deveRetornarDTO() {
        EmpresaEntity entity = buildEntity(10L, "Empresa X", "12345678000199");
        when(empresaRepository.findById(10L)).thenReturn(Optional.of(entity));

        EmpresaResponse result = empresaService.buscar(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.nome()).isEqualTo("Empresa X");
    }

    @Test
    void buscar_quandoNaoEncontrado_deveLancarResourceNotFoundException() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.buscar(99L));
    }

    private EmpresaEntity buildEntity(Long id, String nome, String cnpj) {
        EmpresaEntity entity = org.mockito.Mockito.mock(EmpresaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getNome()).thenReturn(nome);
        when(entity.getCnpj()).thenReturn(cnpj);
        return entity;
    }
}
