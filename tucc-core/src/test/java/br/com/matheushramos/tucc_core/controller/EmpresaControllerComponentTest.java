package br.com.matheushramos.tucc_core.controller;

import br.com.matheushramos.tucc_core.entity.EmpresaEntity;
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
class EmpresaControllerComponentTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProdutoRepository produtoRepository;

    @MockitoBean
    EmpresaRepository empresaRepository;

    @MockitoBean
    ProdutoEmpresaRepository produtoEmpresaRepository;

    @Test
    void listar_deveRetornarPageComEmpresas() throws Exception {
        EmpresaEntity entity = buildEmpresaMock(10L, "Empresa X", "12345678000199");
        when(empresaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        mockMvc.perform(get("/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].nome").value("Empresa X"))
                .andExpect(jsonPath("$.content[0].cnpj").value("12345678000199"));
    }

    @Test
    void buscar_quandoEncontrado_deveRetornar200ComDTO() throws Exception {
        EmpresaEntity entity = buildEmpresaMock(10L, "Empresa X", "12345678000199");
        when(empresaRepository.findById(10L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/empresas/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Empresa X"));
    }

    @Test
    void buscar_quandoNaoEncontrado_deveRetornar404() throws Exception {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/empresas/99"))
                .andExpect(status().isNotFound());
    }

    private EmpresaEntity buildEmpresaMock(Long id, String nome, String cnpj) {
        EmpresaEntity entity = mock(EmpresaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getNome()).thenReturn(nome);
        when(entity.getCnpj()).thenReturn(cnpj);
        when(entity.getCreatedAt()).thenReturn(null);
        when(entity.getUpdatedAt()).thenReturn(null);
        return entity;
    }
}
