package br.com.matheushramos.tucc_monolito.dao;

import br.com.matheushramos.tucc_monolito.model.Empresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAO {

    public List<Empresa> listar() {
        List<Empresa> lista = new ArrayList<Empresa>();
        String sql = "SELECT id, nome, cnpj, dt_criacao, dt_atualizacao " +
                     "FROM empresa ORDER BY id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar empresas", e);
        }
        return lista;
    }

    public Empresa buscarPorId(Long id) {
        String sql = "SELECT id, nome, cnpj, dt_criacao, dt_atualizacao " +
                     "FROM empresa WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar empresa id=" + id, e);
        }
        return null;
    }

    public void inserir(Empresa empresa) {
        String sql = "INSERT INTO empresa (nome, cnpj, dt_criacao, dt_atualizacao) " +
                     "VALUES (?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empresa.getNome());
            stmt.setString(2, empresa.getCnpj());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir empresa", e);
        }
    }

    public void atualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nome = ?, cnpj = ?, " +
                     "dt_atualizacao = SYSTIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empresa.getNome());
            stmt.setString(2, empresa.getCnpj());
            stmt.setLong(3, empresa.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar empresa id=" + empresa.getId(), e);
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM empresa WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir empresa id=" + id, e);
        }
    }

    private Empresa mapRow(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getLong("id"));
        e.setNome(rs.getString("nome"));
        e.setCnpj(rs.getString("cnpj"));
        Timestamp dtCriacao = rs.getTimestamp("dt_criacao");
        if (dtCriacao != null) e.setDtCriacao(new java.util.Date(dtCriacao.getTime()));
        Timestamp dtAtualizacao = rs.getTimestamp("dt_atualizacao");
        if (dtAtualizacao != null) e.setDtAtualizacao(new java.util.Date(dtAtualizacao.getTime()));
        return e;
    }
}
