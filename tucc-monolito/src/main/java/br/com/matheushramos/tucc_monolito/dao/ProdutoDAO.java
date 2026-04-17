package br.com.matheushramos.tucc_monolito.dao;

import br.com.matheushramos.tucc_monolito.model.Produto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public List<Produto> listar() {
        List<Produto> lista = new ArrayList<Produto>();
        String sql = "SELECT id, nome, valor, desconto, dt_criacao, dt_atualizacao " +
                     "FROM produto ORDER BY id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos", e);
        }
        return lista;
    }

    public Produto buscarPorId(Long id) {
        String sql = "SELECT id, nome, valor, desconto, dt_criacao, dt_atualizacao " +
                     "FROM produto WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto id=" + id, e);
        }
        return null;
    }

    public void inserir(Produto produto) {
        String sql = "INSERT INTO produto (nome, valor, desconto, dt_criacao, dt_atualizacao) " +
                     "VALUES (?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getValor());
            setBigDecimalNullable(stmt, 3, produto.getDesconto());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }

    public void atualizar(Produto produto) {
        String sql = "UPDATE produto SET nome = ?, valor = ?, desconto = ?, " +
                     "dt_atualizacao = SYSTIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getValor());
            setBigDecimalNullable(stmt, 3, produto.getDesconto());
            stmt.setLong(4, produto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto id=" + produto.getId(), e);
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto id=" + id, e);
        }
    }

    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setValor(rs.getBigDecimal("valor"));
        p.setDesconto(rs.getBigDecimal("desconto"));
        Timestamp dtCriacao = rs.getTimestamp("dt_criacao");
        if (dtCriacao != null) p.setDtCriacao(new java.util.Date(dtCriacao.getTime()));
        Timestamp dtAtualizacao = rs.getTimestamp("dt_atualizacao");
        if (dtAtualizacao != null) p.setDtAtualizacao(new java.util.Date(dtAtualizacao.getTime()));
        return p;
    }

    private void setBigDecimalNullable(PreparedStatement stmt, int idx, BigDecimal value)
            throws SQLException {
        if (value != null) {
            stmt.setBigDecimal(idx, value);
        } else {
            stmt.setNull(idx, Types.NUMERIC);
        }
    }
}
