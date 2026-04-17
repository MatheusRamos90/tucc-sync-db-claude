package br.com.matheushramos.tucc_monolito.action;

import br.com.matheushramos.tucc_monolito.dao.ProdutoDAO;
import br.com.matheushramos.tucc_monolito.model.Produto;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.util.List;
import java.util.Map;

public class ProdutoAction extends ActionSupport implements SessionAware {

    private static final long serialVersionUID = 1L;

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    private List<Produto> produtos;
    private Produto produto = new Produto();
    private Long id;
    private Map<String, Object> session;

    // --- Actions ---

    public String listar() {
        produtos = produtoDAO.listar();
        return SUCCESS;
    }

    public String novo() {
        produto = new Produto();
        return SUCCESS;
    }

    public String editar() {
        produto = produtoDAO.buscarPorId(id);
        if (produto == null) {
            addActionError("Produto não encontrado.");
            return ERROR;
        }
        return SUCCESS;
    }

    public String salvar() {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            addFieldError("produto.nome", "Nome é obrigatório.");
            return INPUT;
        }
        if (produto.getValor() == null) {
            addFieldError("produto.valor", "Valor é obrigatório.");
            return INPUT;
        }
        if (produto.getId() == null) {
            produtoDAO.inserir(produto);
            session.put("flashMessage", "Produto cadastrado com sucesso!");
            session.put("flashType", "success");
        } else {
            produtoDAO.atualizar(produto);
            session.put("flashMessage", "Produto atualizado com sucesso!");
            session.put("flashType", "success");
        }
        return SUCCESS;
    }

    public String excluir() {
        try {
            produtoDAO.excluir(id);
            session.put("flashMessage", "Produto excluído com sucesso!");
            session.put("flashType", "success");
        } catch (RuntimeException e) {
            session.put("flashMessage", "Não foi possível excluir o produto. Verifique se ele está associado a alguma empresa.");
            session.put("flashType", "danger");
        }
        return SUCCESS;
    }

    // --- Getters / Setters ---

    public List<Produto> getProdutos() { return produtos; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public void setSession(Map<String, Object> session) { this.session = session; }
}
