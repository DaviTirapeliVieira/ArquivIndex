package dao;

import model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public Categoria inserir(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nome, cor) VALUES (?, ?)";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getCor());

            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    categoria.setId(chaves.getInt(1));
                }
            }
        }
        return categoria;
    }

    @Override
    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT id, nome, cor FROM categoria ORDER BY nome";
        List<Categoria> lista = new ArrayList<>();
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(montarCategoria(rs));
            }
        }
        return lista;
    }

    @Override
    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, cor FROM categoria WHERE id = ?";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarCategoria(rs);
                }
            }
        }
        return null;
    }

    private Categoria montarCategoria(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getString("cor"));
    }
}
