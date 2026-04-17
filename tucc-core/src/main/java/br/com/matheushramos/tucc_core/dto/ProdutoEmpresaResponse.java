package br.com.matheushramos.tucc_core.dto;

import br.com.matheushramos.tucc_core.entity.ProdutoEmpresaEntity;

public record ProdutoEmpresaResponse(
    Long id,
    Long produtoId,
    String produtoNome,
    Long empresaId,
    String empresaNome
) {
    public static ProdutoEmpresaResponse from(ProdutoEmpresaEntity e) {
        return new ProdutoEmpresaResponse(
                e.getId(),
                e.getProdutoId(),
                e.getProduto().getNome(),
                e.getEmpresaId(),
                e.getEmpresa().getNome());
    }
}
