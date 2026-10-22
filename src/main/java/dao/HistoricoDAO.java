package dao;

import model.HistoricoAlteracao;
import java.sql.SQLException;
import java.util.List;

public interface HistoricoDAO {

    HistoricoAlteracao inserir(HistoricoAlteracao historico) throws SQLException;

    List<HistoricoAlteracao> listarPorArquivo(int arquivoId) throws SQLException;
}
