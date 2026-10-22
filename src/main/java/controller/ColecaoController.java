package controller;

import dao.ColecaoDAO;
import dao.ColecaoDAOImpl;
import model.Colecao;

import java.sql.SQLException;
import java.util.List;

public class ColecaoController {

    private static final int TAMANHO_MAXIMO_NOME = 100;
    private static final int TAMANHO_MAXIMO_DESCRICAO = 300;

    private final ColecaoDAO dao;

    public ColecaoController() {
        this.dao = new ColecaoDAOImpl();
    }

    public Colecao cadastrar(String nome, String descricao) throws ArquivoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ArquivoException("O nome da coleção não pode ficar em branco.");
        }
        if (nome.trim().length() > TAMANHO_MAXIMO_NOME) {
            throw new ArquivoException("O nome da coleção não pode ter mais que " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        if (descricao != null && descricao.trim().length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ArquivoException("A descrição da coleção não pode ter mais que " + TAMANHO_MAXIMO_DESCRICAO + " caracteres.");
        }

        Colecao nova = new Colecao(nome.trim(), descricao == null ? "" : descricao.trim());
        try {
            return dao.inserir(nova);
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
    }

    public List<Colecao> listarTodas() throws ArquivoException {
        try {
            return dao.listarTodas();
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
    }

    private ArquivoException traduzirErroBanco(SQLException e) {
        String estadoSql = e.getSQLState();
        if (estadoSql != null && estadoSql.startsWith("08")) {
            return new ArquivoException(
                    "Não foi possível conectar ao banco de dados. Verifique se o MySQL está em execução.", e);
        }
        if ("28000".equals(estadoSql)) {
            return new ArquivoException(
                    "Usuário ou senha do banco de dados estão incorretos. Verifique o arquivo config.properties.", e);
        }
        // 23000 é o SQLState padrão para violação de restrição — aqui, o
        // UNIQUE no nome da coleção.
        if ("23000".equals(estadoSql)) {
            return new ArquivoException("Já existe uma coleção com esse nome.", e);
        }
        return new ArquivoException("Ocorreu um erro ao acessar o banco de dados: " + e.getMessage(), e);
    }
}
