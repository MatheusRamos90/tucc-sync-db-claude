package br.com.matheushramos.tucc_sync_db_consumer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "empresa")
public class EmpresaEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 18, nullable = false)
    private String cnpj;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
