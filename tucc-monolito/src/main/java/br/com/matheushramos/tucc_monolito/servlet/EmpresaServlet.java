package br.com.matheushramos.tucc_monolito.servlet;

import br.com.matheushramos.tucc_monolito.dao.EmpresaDAO;
import br.com.matheushramos.tucc_monolito.model.Empresa;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmpresaServlet extends HttpServlet {

    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                Empresa empresa = empresaDAO.buscarPorId(Long.parseLong(idParam));
                if (empresa == null) {
                    resp.setStatus(404);
                    mapper.writeValue(resp.getWriter(), errorMap("Empresa não encontrada"));
                } else {
                    mapper.writeValue(resp.getWriter(), empresa);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), errorMap("ID inválido"));
            }
        } else {
            mapper.writeValue(resp.getWriter(), empresaDAO.listar());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);

            String nome = (String) body.get("nome");
            if (nome == null || nome.trim().isEmpty()) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), errorMap("Nome é obrigatório"));
                return;
            }

            Empresa empresa = new Empresa();
            Object idObj = body.get("id");
            if (idObj != null) {
                empresa.setId(toLong(idObj));
            }
            empresa.setNome(nome.trim());

            Object cnpjObj = body.get("cnpj");
            if (cnpjObj != null && !cnpjObj.toString().trim().isEmpty()) {
                empresa.setCnpj(cnpjObj.toString().trim());
            }

            if (empresa.getId() == null) {
                empresaDAO.inserir(empresa);
                mapper.writeValue(resp.getWriter(), successMap("Empresa cadastrada com sucesso!"));
            } else {
                empresaDAO.atualizar(empresa);
                mapper.writeValue(resp.getWriter(), successMap("Empresa atualizada com sucesso!"));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            mapper.writeValue(resp.getWriter(), errorMap("Erro interno: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(400);
            mapper.writeValue(resp.getWriter(), errorMap("ID é obrigatório"));
            return;
        }
        try {
            empresaDAO.excluir(Long.parseLong(idParam));
            mapper.writeValue(resp.getWriter(), successMap("Empresa excluída com sucesso!"));
        } catch (RuntimeException e) {
            resp.setStatus(409);
            mapper.writeValue(resp.getWriter(),
                    errorMap("Não foi possível excluir a empresa. Verifique se ela está associada a algum produto."));
        }
    }

    private Long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString().trim());
    }

    private Map<String, Object> successMap(String message) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("success", true);
        m.put("message", message);
        return m;
    }

    private Map<String, Object> errorMap(String message) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }
}
