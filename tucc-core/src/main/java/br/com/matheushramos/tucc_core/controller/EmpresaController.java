package br.com.matheushramos.tucc_core.controller;

import br.com.matheushramos.tucc_core.dto.EmpresaResponse;
import br.com.matheushramos.tucc_core.service.EmpresaService;
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
@RequestMapping("/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Consulta de empresas sincronizadas do Oracle")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    @Operation(summary = "Lista empresas com paginação")
    public Page<EmpresaResponse> listar(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return empresaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca empresa por ID")
    @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    public EmpresaResponse buscar(@PathVariable Long id) {
        return empresaService.buscar(id);
    }
}
