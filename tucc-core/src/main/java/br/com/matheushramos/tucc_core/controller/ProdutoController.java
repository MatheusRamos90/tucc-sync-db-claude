package br.com.matheushramos.tucc_core.controller;

import br.com.matheushramos.tucc_core.dto.ProdutoResponse;
import br.com.matheushramos.tucc_core.service.ProdutoService;
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
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Consulta de produtos sincronizados do Oracle")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    @Operation(summary = "Lista produtos com paginação")
    public Page<ProdutoResponse> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return produtoService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto por ID")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return produtoService.buscar(id);
    }
}
