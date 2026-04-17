package br.com.matheushramos.tucc_core.controller;

import br.com.matheushramos.tucc_core.dto.ProdutoEmpresaResponse;
import br.com.matheushramos.tucc_core.service.ProdutoEmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/produtos-empresas")
@RequiredArgsConstructor
@Tag(name = "Produtos-Empresas", description = "Consulta de associações produto-empresa sincronizadas do Oracle")
public class ProdutoEmpresaController {

    private final ProdutoEmpresaService produtoEmpresaService;

    @GetMapping
    @Operation(summary = "Lista associações produto-empresa com paginação")
    public Page<ProdutoEmpresaResponse> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return produtoEmpresaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca associação produto-empresa por ID")
    @ApiResponse(responseCode = "404", description = "Associação não encontrada")
    public ProdutoEmpresaResponse buscar(@PathVariable Long id) {
        return produtoEmpresaService.buscar(id);
    }
}
