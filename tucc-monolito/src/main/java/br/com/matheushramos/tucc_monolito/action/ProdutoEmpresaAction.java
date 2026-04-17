package br.com.matheushramos.tucc_monolito.action;

import br.com.matheushramos.tucc_monolito.dao.EmpresaDAO;
import br.com.matheushramos.tucc_monolito.dao.ProdutoDAO;
import br.com.matheushramos.tucc_monolito.dao.ProdutoEmpresaDAO;
import br.com.matheushramos.tucc_monolito.model.Empresa;
import br.com.matheushramos.tucc_monolito.model.Produto;
import br.com.matheushramos.tucc_monolito.model.ProdutoEmpresa;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.util.List;
import java.util.Map;

public class ProdutoEmpresaAction extends ActionSupport implements SessionAware {

    private static final long serialVersionUID = 1L;

    private final ProdutoEmpresaDAO produtoEmpresaDAO = new ProdutoEmpresaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    private List<ProdutoEmpresa> associacoes;
    private ProdutoEmpresa produtoEmpresa = new ProdutoEmpresa();
    private List<Produto> produtos;
    private List<Empresa> empresas;
    private Long id;
    private Map<String, Object> session;

    // --- Actions ---

    public String listar() {
        associacoes = produtoEmpresaDAO.listar();
        return SUCCESS;
    }

    public String novo() {
        produtoEmpresa = new ProdutoEmpresa();
        produtos = produtoDAO.listar();
        empresas = empresaDAO.listar();
        return SUCCESS;
    }

    public String salvar() {
        if (produtoEmpresa.getProdutoId() == null) {
            addFieldError("produtoEmpresa.produtoId", "Produto é obrigatório.");
        }
        if (produtoEmpresa.getEmpresaId() == null) {
            addFieldError("produtoEmpresa.empresaId", "Empresa é obrigatória.");
        }
        if (hasFieldErrors()) {
            produtos = produtoDAO.listar();
            empresas = empresaDAO.listar();
            return INPUT;
        }
        produtoEmpresaDAO.inserir(produtoEmpresa);
        session.put("flashMessage", "Associação cadastrada com sucesso!");
        session.put("flashType", "success");
        return SUCCESS;
    }

    public String excluir() {
        produtoEmpresaDAO.excluir(id);
        session.put("flashMessage", "Associação excluída com sucesso!");
        session.put("flashType", "success");
        return SUCCESS;
    }

    // --- Getters / Setters ---

    public List<ProdutoEmpresa> getAssociacoes() { return associacoes; }
    public ProdutoEmpresa getProdutoEmpresa() { return produtoEmpresa; }
    public void setProdutoEmpresa(ProdutoEmpresa produtoEmpresa) { this.produtoEmpresa = produtoEmpresa; }
    public List<Produto> getProdutos() { return produtos; }
    public List<Empresa> getEmpresas() { return empresas; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public void setSession(Map<String, Object> session) { this.session = session; }
}
