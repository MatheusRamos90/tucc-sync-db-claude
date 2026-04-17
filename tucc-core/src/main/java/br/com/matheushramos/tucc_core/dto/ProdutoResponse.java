package br.com.matheushramos.tucc_core.dto;

import br.com.matheushramos.tucc_core.entity.ProdutoEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record ProdutoResponse(
    Long id,
    String nome,
    BigDecimal valor,
    BigDecimal desconto,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProdutoResponse from(ProdutoEntity e) {
        return new ProdutoResponse(
                e.getId(), e.getNome(), e.getValor(),
                e.getDesconto(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
