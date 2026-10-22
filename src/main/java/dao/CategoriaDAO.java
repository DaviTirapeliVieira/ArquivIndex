package dao;

import model.Categoria;
import java.sql.SQLException;
import java.util.List;

public interface CategoriaDAO {

    Categoria inserir(Categoria categoria) throws SQLException;

    List<Categoria> listarTodas() throws SQLException;

    Categoria buscarPorId(int id) throws SQLException;
}
