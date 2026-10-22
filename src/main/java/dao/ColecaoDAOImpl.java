package dao;

import model.Colecao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ColecaoDAOImpl implements ColecaoDAO {

    @Override
    public Colecao inserir(Colecao colecao) throws SQLException {
        String sql = "INSERT INTO colecao (nome, descricao, data_criacao) VALUES (?, ?, ?)";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, colecao.getNome());
            stmt.setString(2, colecao.getDescricao());
            stmt.setTimestamp(3, Timestamp.valueOf(colecao.getDataCriacao()));

            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    colecao.setId(chaves.getInt(1));
                }
            }
        }
        return colecao;
    }

    @Override
    public List<Colecao> listarTodas() throws SQLException {
        String sql = "SELECT id, nome, descricao, data_criacao FROM colecao ORDER BY nome";
        List<Colecao> lista = new ArrayList<>();
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(montarColecao(rs));
            }
        }
        return lista;
    }

    @Override
    public Colecao buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, descricao, data_criacao FROM colecao WHERE id = ?";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarColecao(rs);
                }
            }
        }
        return null;
    }

    private Colecao montarColecao(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String descricao = rs.getString("descricao");
        LocalDateTime dataCriacao = rs.getTimestamp("data_criacao").toLocalDateTime();
        return new Colecao(id, nome, descricao, dataCriacao);
    }
}
