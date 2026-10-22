package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Responsável por UMA única coisa: fornecer a conexão com o banco de dados.
 *
 * Padrão Singleton: existe apenas UMA instância desta classe durante toda a
 * execução do programa. Toda parte do sistema que precisar de conexão pede
 * pra ela, em vez de cada DAO abrir sua própria conexão separada.
 *
 * A URL, o usuário e a senha NÃO ficam fixos no código: vêm de
 * config.properties (src/main/resources), lido do classpath. Isso permite
 * levar o projeto para outra máquina só ajustando um arquivo de texto, sem
 * recompilar (ver §12.1 da especificação).
 */
public class ConexaoBD {

    private static final String ARQUIVO_CONFIG = "config.properties";

    // Começa nula; só é criada na primeira vez que alguém pedir (lazy initialization).
    private static ConexaoBD instancia;

    private final String url;
    private final String usuario;
    private final String senha;

    private Connection conexao;

    // Construtor PRIVATE: ninguém de fora pode escrever "new ConexaoBD()".
    private ConexaoBD() throws SQLException {
        Properties config = carregarConfig();
        this.url = obterPropriedadeObrigatoria(config, "db.url");
        this.usuario = obterPropriedadeObrigatoria(config, "db.usuario");
        this.senha = obterPropriedadeObrigatoria(config, "db.senha");
        this.conexao = DriverManager.getConnection(url, usuario, senha);
    }

    public static ConexaoBD getInstancia() throws SQLException {
        if (instancia == null) {
            instancia = new ConexaoBD();
        }
        // Se a conexão caiu (banco reiniciado, timeout, etc.), reabre em vez de
        // devolver uma conexão morta para o resto do sistema.
        if (instancia.conexao == null || instancia.conexao.isClosed()) {
            instancia.conexao = DriverManager.getConnection(instancia.url, instancia.usuario, instancia.senha);
        }
        return instancia;
    }

    public Connection getConexao() {
        return conexao;
    }

    // getResourceAsStream() procura o arquivo dentro do classpath — é assim
    // que um .properties colocado em src/main/resources é encontrado depois
    // de compilado, esteja o programa rodando de dentro do target/classes ou
    // de um .jar.
    private Properties carregarConfig() {
        Properties propriedades = new Properties();
        try (InputStream entrada = getClass().getClassLoader().getResourceAsStream(ARQUIVO_CONFIG)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "Arquivo '" + ARQUIVO_CONFIG + "' não encontrado. Crie o arquivo " +
                        "src/main/resources/" + ARQUIVO_CONFIG + " com as chaves db.url, " +
                        "db.usuario e db.senha antes de executar o programa.");
            }
            propriedades.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível ler o arquivo '" + ARQUIVO_CONFIG + "': " + e.getMessage(), e);
        }
        return propriedades;
    }

    // Falha cedo e com mensagem específica de qual chave falta, em vez de
    // deixar o programa tentar conectar com uma URL/usuário/senha nulos e
    // dar um erro técnico confuso mais adiante (ver §12.1: "não falhar
    // silenciosamente").
    private String obterPropriedadeObrigatoria(Properties config, String chave) {
        String valor = config.getProperty(chave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "A propriedade obrigatória '" + chave + "' está ausente ou vazia em '" +
                    ARQUIVO_CONFIG + "'. Verifique o arquivo src/main/resources/" + ARQUIVO_CONFIG + ".");
        }
        return valor;
    }
}
