package dao;

import model.HistoricoAlteracao;
import model.TipoOperacao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAOImpl implements HistoricoDAO {

    @Override
    public HistoricoAlteracao inserir(HistoricoAlteracao historico) throws SQLException {
        String sql = "INSERT INTO historico_alteracao " +
                "(arquivo_id, campo_alterado, valor_anterior, valor_novo, tipo_operacao, data_alteracao) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, historico.getArquivoId());
            stmt.setString(2, historico.getCampoAlterado());
            stmt.setString(3, historico.getValorAnterior());
            stmt.setString(4, historico.getValorNovo());
            stmt.setString(5, historico.getTipoOperacao().name());
            stmt.setTimestamp(6, Timestamp.valueOf(historico.getDataAlteracao()));

            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    historico.setId(chaves.getInt(1));
                }
            }
        }
        return historico;
    }

    @Override
    public List<HistoricoAlteracao> listarPorArquivo(int arquivoId) throws SQLException {
        String sql = "SELECT id, arquivo_id, campo_alterado, valor_anterior, valor_novo, " +
                "tipo_operacao, data_alteracao FROM historico_alteracao " +
                "WHERE arquivo_id = ? ORDER BY data_alteracao";

        List<HistoricoAlteracao> lista = new ArrayList<>();
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, arquivoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int arqId = rs.getInt("arquivo_id");
                    String campo = rs.getString("campo_alterado");
                    String valorAnterior = rs.getString("valor_anterior");
                    String valorNovo = rs.getString("valor_novo");
                    TipoOperacao tipo = TipoOperacao.valueOf(rs.getString("tipo_operacao"));
                    LocalDateTime data = rs.getTimestamp("data_alteracao").toLocalDateTime();

                    lista.add(new HistoricoAlteracao(id, arqId, campo, valorAnterior, valorNovo, tipo, data));
                }
            }
        }
        return lista;
    }
}
