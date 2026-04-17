package br.com.matheushramos.tucc_core.controller;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de componente: passa por todas as camadas (Controller → Service → Repository mock)
 * usando H2 para o JpaTransactionManager necessário ao @Transactional(readOnly=true) do Service.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:testcoredb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc
class ProdutoControllerComponentTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProdutoRepository produtoRepository;

    @MockitoBean
    EmpresaRepository empresaRepository;

    @MockitoBean
    ProdutoEmpresaRepository produtoEmpresaRepository;

    @Test
    void listar_deveRetornarPageComProdutos() throws Exception {
        ProdutoEntity entity = buildProdutoMock(1L, "Produto A", "99.99");
        when(produtoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Produto A"));
    }

    @Test
    void buscar_quandoEncontrado_deveRetornar200ComDTO() throws Exception {
        ProdutoEntity entity = buildProdutoMock(1L, "Produto A", "99.99");
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Produto A"));
    }

    @Test
    void buscar_quandoNaoEncontrado_deveRetornar404() throws Exception {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound());
    }

    private ProdutoEntity buildProdutoMock(Long id, String nome, String valor) {
        ProdutoEntity entity = mock(ProdutoEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getNome()).thenReturn(nome);
        when(entity.getValor()).thenReturn(new BigDecimal(valor));
        when(entity.getDesconto()).thenReturn(null);
        when(entity.getCreatedAt()).thenReturn(null);
        when(entity.getUpdatedAt()).thenReturn(null);
        return entity;
    }
}
