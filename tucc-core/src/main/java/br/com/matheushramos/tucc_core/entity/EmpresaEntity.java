package br.com.matheushramos.tucc_core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "empresa")
public class EmpresaEntity {

    @Id
    private Long id;

    private String nome;

    @Column(length = 18)
    private String cnpj;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
