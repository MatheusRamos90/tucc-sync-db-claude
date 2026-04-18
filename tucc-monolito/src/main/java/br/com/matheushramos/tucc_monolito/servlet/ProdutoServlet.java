package br.com.matheushramos.tucc_monolito.servlet;

import br.com.matheushramos.tucc_monolito.dao.ProdutoDAO;
import br.com.matheushramos.tucc_monolito.model.Produto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ProdutoServlet extends HttpServlet {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                Produto produto = produtoDAO.buscarPorId(Long.parseLong(idParam));
                if (produto == null) {
                    resp.setStatus(404);
                    mapper.writeValue(resp.getWriter(), errorMap("Produto não encontrado"));
                } else {
                    mapper.writeValue(resp.getWriter(), produto);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), errorMap("ID inválido"));
            }
        } else {
            mapper.writeValue(resp.getWriter(), produtoDAO.listar());
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

            Object valorObj = body.get("valor");
            if (valorObj == null || valorObj.toString().trim().isEmpty()) {
                resp.setStatus(400);
                mapper.writeValue(resp.getWriter(), errorMap("Valor é obrigatório"));
                return;
            }

            Produto produto = new Produto();
            Object idObj = body.get("id");
            if (idObj != null) {
                produto.setId(toLong(idObj));
            }
            produto.setNome(nome.trim());
            produto.setValor(toBigDecimal(valorObj));

            Object descontoObj = body.get("desconto");
            if (descontoObj != null && !descontoObj.toString().trim().isEmpty()) {
                produto.setDesconto(toBigDecimal(descontoObj));
            }

            if (produto.getId() == null) {
                produtoDAO.inserir(produto);
                mapper.writeValue(resp.getWriter(), successMap("Produto cadastrado com sucesso!"));
            } else {
                produtoDAO.atualizar(produto);
                mapper.writeValue(resp.getWriter(), successMap("Produto atualizado com sucesso!"));
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
            produtoDAO.excluir(Long.parseLong(idParam));
            mapper.writeValue(resp.getWriter(), successMap("Produto excluído com sucesso!"));
        } catch (RuntimeException e) {
            resp.setStatus(409);
            mapper.writeValue(resp.getWriter(),
                    errorMap("Não foi possível excluir o produto. Verifique se ele está associado a alguma empresa."));
        }
    }

    private Long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString().trim());
    }

    private BigDecimal toBigDecimal(Object obj) {
        String s = obj.toString().trim().replace(",", ".");
        return new BigDecimal(s);
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
