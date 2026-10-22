package controller;

import java.sql.SQLException;
import java.util.List;

import dao.CategoriaDAO;
import dao.CategoriaDAOImpl;
import model.Categoria;

public class CategoriaController {

    private static final int TAMANHO_MAXIMO_NOME = 60;
    private static final int TAMANHO_MAXIMO_COR = 7;

    private final CategoriaDAO dao;

    public CategoriaController() {
        this.dao = new CategoriaDAOImpl();
    }

    public Categoria cadastrar(String nome, String cor) throws ArquivoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ArquivoException("O nome da categoria não pode ficar em branco.");
        }
        if (nome.trim().length() > TAMANHO_MAXIMO_NOME) {
            throw new ArquivoException("O nome da categoria não pode ter mais que " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        if (cor != null && cor.trim().length() > TAMANHO_MAXIMO_COR) {
            throw new ArquivoException("A cor deve estar no formato hexadecimal, ex.: #EF4444.");
        }

        if(cor != null && !cor.isBlank() && !cor.trim().matches("^#[0-9A-Fa-f]{6}$")){
            throw new ArquivoException("A cor deve estar no formato hexadecimal, ex: #EF4444");
        }

        Categoria nova = new Categoria(nome.trim(), cor == null || cor.isBlank() ? null : cor.trim());
        try {
            return dao.inserir(nova);
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
    }

    public List<Categoria> listarTodas() throws ArquivoException {
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
        if ("23000".equals(estadoSql)) {
            return new ArquivoException("Já existe uma categoria com esse nome.", e);
        }
        return new ArquivoException("Ocorreu um erro ao acessar o banco de dados: " + e.getMessage(), e);
    }
}
