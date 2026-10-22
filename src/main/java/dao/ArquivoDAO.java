package dao;

import controller.FiltroBusca;
import model.Arquivo;
import model.Categoria;

import java.sql.SQLException;
import java.util.List;

/**
 * O padrão DAO separa "o que pode ser feito com os dados" (esta interface)
 * de "como isso é feito de verdade" (ArquivoDAOImpl). Se o banco mudar,
 * só a implementação muda — quem usa o DAO não percebe.
 */
public interface ArquivoDAO {

    Arquivo inserir(Arquivo arquivo, List<Integer> categoriaIds) throws SQLException;

    void atualizar(Arquivo arquivo) throws SQLException;

    List<Arquivo> listarTodos() throws SQLException;

    Arquivo buscarPorId(int id) throws SQLException;

    List<Arquivo> buscar(FiltroBusca filtro) throws SQLException;

    void vincularCategoria(int arquivoId, int categoriaId) throws SQLException;

    void desvincularCategoria(int arquivoId, int categoriaId) throws SQLException;

    List<Categoria> listarCategoriasDoArquivo(int arquivoId) throws SQLException;
}
