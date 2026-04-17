package br.com.matheushramos.tucc_monolito.model;

public class ProdutoEmpresa {

    private Long id;
    private Long produtoId;
    private Long empresaId;

    /** Preenchido via JOIN no DAO — somente leitura */
    private String produtoNome;
    /** Preenchido via JOIN no DAO — somente leitura */
    private String empresaNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public String getProdutoNome() { return produtoNome; }
    public void setProdutoNome(String produtoNome) { this.produtoNome = produtoNome; }

    public String getEmpresaNome() { return empresaNome; }
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }
}
