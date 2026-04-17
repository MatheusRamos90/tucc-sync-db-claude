package br.com.matheushramos.tucc_core.dto;

import br.com.matheushramos.tucc_core.entity.EmpresaEntity;

import java.time.Instant;

public record EmpresaResponse(
    Long id,
    String nome,
    String cnpj,
    Instant createdAt,
    Instant updatedAt
) {
    public static EmpresaResponse from(EmpresaEntity e) {
        return new EmpresaResponse(
                e.getId(), e.getNome(), e.getCnpj(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
