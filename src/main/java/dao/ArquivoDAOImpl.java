package dao;

import controller.FiltroBusca;
import model.Arquivo;
import model.Categoria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArquivoDAOImpl implements ArquivoDAO {

    @Override
    public Arquivo inserir(Arquivo arquivo, List<Integer> categoriaIds) throws SQLException {
        String sql = "INSERT INTO arquivo " +
                "(nome, tipo, descricao, caminho, favorito, data_criacao, data_modificacao, colecao_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conexao = ConexaoBD.getInstancia().getConexao();

        // Transação: o arquivo e seus vínculos de categoria precisam ser
        // salvos juntos. Se o vínculo falhar no meio do caminho, desfazemos
        // tudo com rollback() para não deixar um arquivo salvo com
        // categorias pela metade (ver §7.2 da especificação).
        conexao.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, arquivo.getNome());
                stmt.setString(2, arquivo.getTipo());
                stmt.setString(3, arquivo.getDescricao());
                stmt.setString(4, arquivo.getCaminho());
                stmt.setBoolean(5, arquivo.isFavorito());
                stmt.setTimestamp(6, Timestamp.valueOf(arquivo.getDataCriacao()));
                stmt.setTimestamp(7, Timestamp.valueOf(arquivo.getDataModificacao()));
                setColecaoId(stmt, 8, arquivo.getColecaoId());

                stmt.executeUpdate();

                try (ResultSet chaves = stmt.getGeneratedKeys()) {
                    if (chaves.next()) {
                        arquivo.setId(chaves.getInt(1));
                    }
                }
            }

            if (categoriaIds != null) {
                for (int categoriaId : categoriaIds) {
                    vincularCategoria(arquivo.getId(), categoriaId);
                }
            }

            conexao.commit();
        } catch (SQLException e) {
            conexao.rollback();
            throw e;
        } finally {
            conexao.setAutoCommit(true);
        }

        return arquivo;
    }

    @Override
    public void atualizar(Arquivo arquivo) throws SQLException {
        String sql = "UPDATE arquivo SET nome = ?, tipo = ?, descricao = ?, caminho = ?, " +
                "favorito = ?, data_modificacao = ?, colecao_id = ? WHERE id = ?";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, arquivo.getNome());
            stmt.setString(2, arquivo.getTipo());
            stmt.setString(3, arquivo.getDescricao());
            stmt.setString(4, arquivo.getCaminho());
            stmt.setBoolean(5, arquivo.isFavorito());
            stmt.setTimestamp(6, Timestamp.valueOf(arquivo.getDataModificacao()));
            setColecaoId(stmt, 7, arquivo.getColecaoId());
            stmt.setInt(8, arquivo.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public List<Arquivo> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, tipo, descricao, caminho, favorito, data_criacao, " +
                "data_modificacao, colecao_id FROM arquivo";

        List<Arquivo> lista = new ArrayList<>();
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(montarArquivo(rs));
            }
        }

        preencherCategorias(lista);
        return lista;
    }

    @Override
    public Arquivo buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, tipo, descricao, caminho, favorito, data_criacao, " +
                "data_modificacao, colecao_id FROM arquivo WHERE id = ?";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Arquivo arquivo = montarArquivo(rs);
                    arquivo.setCategorias(listarCategoriasDoArquivo(arquivo.getId()));
                    return arquivo;
                }
            }
        }
        return null;
    }

    /**
     * Monta a consulta de busca combinada aos poucos: começa com um SQL fixo
     * e vai acrescentando "AND ..." conforme cada filtro estiver preenchido,
     * guardando os valores numa lista para setar no PreparedStatement na
     * mesma ordem depois. Filtrar aqui, no banco, evita trazer para a
     * memória registros que a busca descartaria de qualquer forma — ver
     * §7.4 da especificação (filtrar no SQL, ordenar na aplicação).
     */
    @Override
    public List<Arquivo> buscar(FiltroBusca filtro) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT a.id, a.nome, a.tipo, a.descricao, a.caminho, a.favorito, " +
                "a.data_criacao, a.data_modificacao, a.colecao_id FROM arquivo a");

        List<Object> parametros = new ArrayList<>();

        if (filtro.getCategoriaId() != null) {
            sql.append(" JOIN arquivo_categoria ac ON ac.arquivo_id = a.id");
        }

        sql.append(" WHERE 1 = 1");

        if (filtro.getTexto() != null && !filtro.getTexto().isBlank()) {
            sql.append(" AND (a.nome LIKE ? OR a.descricao LIKE ?)");
            String curinga = "%" + filtro.getTexto().trim() + "%";
            parametros.add(curinga);
            parametros.add(curinga);
        }
        if (filtro.getTipo() != null && !filtro.getTipo().isBlank()) {
            sql.append(" AND a.tipo = ?");
            parametros.add(filtro.getTipo().trim());
        }
        if (filtro.getColecaoId() != null) {
            sql.append(" AND a.colecao_id = ?");
            parametros.add(filtro.getColecaoId());
        }
        if (filtro.getCategoriaId() != null) {
            sql.append(" AND ac.categoria_id = ?");
            parametros.add(filtro.getCategoriaId());
        }
        if (filtro.isSomenteFavoritos()) {
            sql.append(" AND a.favorito = TRUE");
        }

        List<Arquivo> lista = new ArrayList<>();
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarArquivo(rs));
                }
            }
        }

        preencherCategorias(lista);
        return lista;
    }

    @Override
    public void vincularCategoria(int arquivoId, int categoriaId) throws SQLException {
        String sql = "INSERT INTO arquivo_categoria (arquivo_id, categoria_id) VALUES (?, ?)";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, arquivoId);
            stmt.setInt(2, categoriaId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void desvincularCategoria(int arquivoId, int categoriaId) throws SQLException {
        String sql = "DELETE FROM arquivo_categoria WHERE arquivo_id = ? AND categoria_id = ?";
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, arquivoId);
            stmt.setInt(2, categoriaId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Categoria> listarCategoriasDoArquivo(int arquivoId) throws SQLException {
        String sql = "SELECT c.id, c.nome, c.cor FROM categoria c " +
                "JOIN arquivo_categoria ac ON ac.categoria_id = c.id " +
                "WHERE ac.arquivo_id = ? ORDER BY c.nome";

        List<Categoria> lista = new ArrayList<>();
        Connection conexao = ConexaoBD.getInstancia().getConexao();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, arquivoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Categoria(rs.getInt("id"), rs.getString("nome"), rs.getString("cor")));
                }
            }
        }
        return lista;
    }

    // Para cada arquivo da lista, busca suas categorias separadamente. Não é
    // a forma mais rápida (um SELECT a mais por arquivo), mas é simples de
    // explicar e a lista de arquivos de um projeto acadêmico nunca é grande
    // o bastante para isso ser um problema real de desempenho.
    private void preencherCategorias(List<Arquivo> lista) throws SQLException {
        for (Arquivo arquivo : lista) {
            arquivo.setCategorias(listarCategoriasDoArquivo(arquivo.getId()));
        }
    }

    // colecao_id aceita NULL no banco (arquivo sem coleção). setNull() é a
    // forma correta de mandar um NULL de verdade via PreparedStatement —
    // stmt.setInt(indice, null) nem compilaria, pois int é primitivo.
    private void setColecaoId(PreparedStatement stmt, int indice, Integer colecaoId) throws SQLException {
        if (colecaoId == null) {
            stmt.setNull(indice, Types.INTEGER);
        } else {
            stmt.setInt(indice, colecaoId);
        }
    }

    private Arquivo montarArquivo(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String tipo = rs.getString("tipo");
        String descricao = rs.getString("descricao");
        String caminho = rs.getString("caminho");
        boolean favorito = rs.getBoolean("favorito");
        LocalDateTime dataCriacao = rs.getTimestamp("data_criacao").toLocalDateTime();
        LocalDateTime dataModificacao = rs.getTimestamp("data_modificacao").toLocalDateTime();

        // getInt() devolve 0 para uma coluna NULL — por isso é preciso
        // checar wasNull() logo em seguida para distinguir "0" de "sem coleção".
        int colecaoIdBruto = rs.getInt("colecao_id");
        Integer colecaoId = rs.wasNull() ? null : colecaoIdBruto;

        return new Arquivo(id, nome, tipo, descricao, caminho, favorito, dataCriacao, dataModificacao, colecaoId);
    }
}
