package br.com.matheushramos.tucc_core.controller;

import br.com.matheushramos.tucc_core.entity.EmpresaEntity;
import br.com.matheushramos.tucc_core.entity.ProdutoEmpresaEntity;
import br.com.matheushramos.tucc_core.entity.ProdutoEntity;
import br.com.matheushramos.tucc_core.repository.EmpresaRepository;
import br.com.matheushramos.tucc_core.repository.ProdutoEmpresaRepository;
import br.com.matheushramos.tucc_core.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:testcoredb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc
class ProdutoEmpresaControllerComponentTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProdutoRepository produtoRepository;

    @MockitoBean
    EmpresaRepository empresaRepository;

    @MockitoBean
    ProdutoEmpresaRepository produtoEmpresaRepository;

    @Test
    void listar_deveRetornarPageComAssociacoes() throws Exception {
        ProdutoEmpresaEntity entity = buildProdutoEmpresaMock(100L, 1L, 10L);
        when(produtoEmpresaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        mockMvc.perform(get("/produtos-empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].produtoId").value(1))
                .andExpect(jsonPath("$.content[0].empresaId").value(10))
                .andExpect(jsonPath("$.content[0].produtoNome").value("Produto A"))
                .andExpect(jsonPath("$.content[0].empresaNome").value("Empresa X"));
    }

    @Test
    void buscar_quandoEncontrado_deveRetornar200ComDTO() throws Exception {
        ProdutoEmpresaEntity entity = buildProdutoEmpresaMock(100L, 1L, 10L);
        when(produtoEmpresaRepository.findById(100L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/produtos-empresas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.produtoNome").value("Produto A"))
                .andExpect(jsonPath("$.empresaNome").value("Empresa X"));
    }

    @Test
    void buscar_quandoNaoEncontrado_deveRetornar404() throws Exception {
        when(produtoEmpresaRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/produtos-empresas/99"))
                .andExpect(status().isNotFound());
    }

    private ProdutoEmpresaEntity buildProdutoEmpresaMock(Long id, Long produtoId, Long empresaId) {
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
