package br.com.matheushramos.tucc_monolito.model;

import java.math.BigDecimal;
import java.util.Date;

public class Produto {

    private Long id;
    private String nome;
    private BigDecimal valor;
    private BigDecimal desconto;
    private Date dtCriacao;
    private Date dtAtualizacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }

    public Date getDtCriacao() { return dtCriacao; }
    public void setDtCriacao(Date dtCriacao) { this.dtCriacao = dtCriacao; }

    public Date getDtAtualizacao() { return dtAtualizacao; }
    public void setDtAtualizacao(Date dtAtualizacao) { this.dtAtualizacao = dtAtualizacao; }
}
