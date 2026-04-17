package br.com.matheushramos.tucc_monolito.dao;

import br.com.matheushramos.tucc_monolito.model.ProdutoEmpresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoEmpresaDAO {

    public List<ProdutoEmpresa> listar() {
        List<ProdutoEmpresa> lista = new ArrayList<ProdutoEmpresa>();
        String sql = "SELECT pe.id, pe.produto_id, pe.empresa_id, " +
                     "       p.nome AS produto_nome, e.nome AS empresa_nome " +
                     "FROM produto_empresa pe " +
                     "JOIN produto p ON pe.produto_id = p.id " +
                     "JOIN empresa e ON pe.empresa_id = e.id " +
                     "ORDER BY pe.id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produto-empresa", e);
        }
        return lista;
    }

    public ProdutoEmpresa buscarPorId(Long id) {
        String sql = "SELECT pe.id, pe.produto_id, pe.empresa_id, " +
                     "       p.nome AS produto_nome, e.nome AS empresa_nome " +
                     "FROM produto_empresa pe " +
                     "JOIN produto p ON pe.produto_id = p.id " +
                     "JOIN empresa e ON pe.empresa_id = e.id " +
                     "WHERE pe.id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto-empresa id=" + id, e);
        }
        return null;
    }

    public void inserir(ProdutoEmpresa pe) {
        String sql = "INSERT INTO produto_empresa (produto_id, empresa_id) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pe.getProdutoId());
            stmt.setLong(2, pe.getEmpresaId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto-empresa", e);
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM produto_empresa WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto-empresa id=" + id, e);
        }
    }

    private ProdutoEmpresa mapRow(ResultSet rs) throws SQLException {
        ProdutoEmpresa pe = new ProdutoEmpresa();
        pe.setId(rs.getLong("id"));
        pe.setProdutoId(rs.getLong("produto_id"));
        pe.setEmpresaId(rs.getLong("empresa_id"));
        pe.setProdutoNome(rs.getString("produto_nome"));
        pe.setEmpresaNome(rs.getString("empresa_nome"));
        return pe;
    }
}
