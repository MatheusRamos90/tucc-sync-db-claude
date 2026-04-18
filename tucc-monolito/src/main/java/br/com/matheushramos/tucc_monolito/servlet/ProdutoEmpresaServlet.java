package br.com.matheushramos.tucc_monolito.servlet;

import br.com.matheushramos.tucc_monolito.dao.EmpresaDAO;
import br.com.matheushramos.tucc_monolito.dao.ProdutoDAO;
import br.com.matheushramos.tucc_monolito.dao.ProdutoEmpresaDAO;
import br.com.matheushramos.tucc_monolito.model.Empresa;
import br.com.matheushramos.tucc_monolito.model.Produto;
import br.com.matheushramos.tucc_monolito.model.ProdutoEmpresa;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProdutoEmpresaServlet extends HttpServlet {

    private final ProdutoEmpresaDAO produtoEmpresaDAO = new ProdutoEmpresaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * GET /api/produto-empresa           → lista todas as associações
     * GET /api/produto-empresa?options=true → {produtos, empresas} para os dropdowns
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        if ("true".equals(req.getParameter("options"))) {
            List<Produto> produtos = produtoDAO.listar();
            List<Empresa> empresas = empresaDAO.listar();

            List<Map<String, Object>> prodOpts = new ArrayList<Map<String, Object>>();
            for (Produto p : produtos) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("id", p.getId());
                item.put("nome", p.getNome());
                prodOpts.add(item);
            }

            List<Map<String, Object>> empOpts = new ArrayList<Map<String, Object>>();
            for (Empresa e : empresas) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("id", e.getId());
                item.put("nome", e.getNome());
                empOpts.add(item);
            }

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("produtos", prodOpts);
            result.put("empresas", empOpts);
            mapper.writeValue(resp.getWriter(), result);
        } else {
            mapper.writeValue(resp.getWriter(), produtoEmpresaDAO.listar());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            Map<?, ?> body = mapper.readValue(req.getInputStream(), Map.class);

            Object produtoIdObj = body.get("produtoId");
            Object empresaIdObj = body.get("empresaId");

            if (produtoIdObj == null) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), errorMap("Produto é obrigatório"));
                return;
            }
            if (empresaIdObj == null) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), errorMap("Empresa é obrigatória"));
                return;
            }

            ProdutoEmpresa pe = new ProdutoEmpresa();
            pe.setProdutoId(toLong(produtoIdObj));
            pe.setEmpresaId(toLong(empresaIdObj));

            produtoEmpresaDAO.inserir(pe);
            mapper.writeValue(resp.getWriter(), successMap("Associação cadastrada com sucesso!"));
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
            produtoEmpresaDAO.excluir(Long.parseLong(idParam));
            mapper.writeValue(resp.getWriter(), successMap("Associação excluída com sucesso!"));
        } catch (RuntimeException e) {
            resp.setStatus(500);
            mapper.writeValue(resp.getWriter(), errorMap("Erro ao excluir associação: " + e.getMessage()));
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
