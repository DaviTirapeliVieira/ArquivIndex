package dao;

import model.Colecao;
import java.sql.SQLException;
import java.util.List;

public interface ColecaoDAO {

    Colecao inserir(Colecao colecao) throws SQLException;

    List<Colecao> listarTodas() throws SQLException;

    Colecao buscarPorId(int id) throws SQLException;
}
