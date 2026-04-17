package br.com.matheushramos.tucc_monolito.model;

import java.util.Date;

public class Empresa {

    private Long id;
    private String nome;
    private String cnpj;
    private Date dtCriacao;
    private Date dtAtualizacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public Date getDtCriacao() { return dtCriacao; }
    public void setDtCriacao(Date dtCriacao) { this.dtCriacao = dtCriacao; }

    public Date getDtAtualizacao() { return dtAtualizacao; }
    public void setDtAtualizacao(Date dtAtualizacao) { this.dtAtualizacao = dtAtualizacao; }
}
