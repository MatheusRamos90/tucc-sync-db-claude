package br.com.matheushramos.tucc_monolito.action;

import br.com.matheushramos.tucc_monolito.dao.EmpresaDAO;
import br.com.matheushramos.tucc_monolito.model.Empresa;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.util.List;
import java.util.Map;

public class EmpresaAction extends ActionSupport implements SessionAware {

    private static final long serialVersionUID = 1L;

    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    private List<Empresa> empresas;
    private Empresa empresa = new Empresa();
    private Long id;
    private Map<String, Object> session;

    // --- Actions ---

    public String listar() {
        empresas = empresaDAO.listar();
        return SUCCESS;
    }

    public String novo() {
        empresa = new Empresa();
        return SUCCESS;
    }

    public String editar() {
        empresa = empresaDAO.buscarPorId(id);
        if (empresa == null) {
            addActionError("Empresa não encontrada.");
            return ERROR;
        }
        return SUCCESS;
    }

    public String salvar() {
        if (empresa.getNome() == null || empresa.getNome().trim().isEmpty()) {
            addFieldError("empresa.nome", "Nome é obrigatório.");
            return INPUT;
        }
        if (empresa.getId() == null) {
            empresaDAO.inserir(empresa);
            session.put("flashMessage", "Empresa cadastrada com sucesso!");
            session.put("flashType", "success");
        } else {
            empresaDAO.atualizar(empresa);
            session.put("flashMessage", "Empresa atualizada com sucesso!");
            session.put("flashType", "success");
        }
        return SUCCESS;
    }

    public String excluir() {
        try {
            empresaDAO.excluir(id);
            session.put("flashMessage", "Empresa excluída com sucesso!");
            session.put("flashType", "success");
        } catch (RuntimeException e) {
            session.put("flashMessage", "Não foi possível excluir a empresa. Verifique se ela está associada a algum produto.");
            session.put("flashType", "danger");
        }
        return SUCCESS;
    }

    // --- Getters / Setters ---

    public List<Empresa> getEmpresas() { return empresas; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public void setSession(Map<String, Object> session) { this.session = session; }
}
