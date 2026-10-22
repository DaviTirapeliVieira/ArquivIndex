package controller;

import dao.ArquivoDAO;
import dao.ArquivoDAOImpl;
import dao.HistoricoDAO;
import dao.HistoricoDAOImpl;
import model.Arquivo;
import model.Categoria;
import model.HistoricoAlteracao;
import model.TipoOperacao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ponte entre a View e o DAO (arquitetura MVC).
 *
 * Responsabilidades: validar o que o usuário digitou, acionar o DAO,
 * registrar histórico automaticamente a cada criação/edição, ordenar a
 * lista antes de devolver para a tela, e traduzir qualquer SQLException em
 * uma ArquivoException com mensagem em português que a View possa exibir
 * direto num JOptionPane.
 */
public class ArquivoController {

    // Limites das colunas no banco (VARCHAR(150)/(50)/(500)/(400)).
    // Validar aqui evita que o banco rejeite o INSERT com um erro técnico.
    private static final int TAMANHO_MAXIMO_NOME = 150;
    private static final int TAMANHO_MAXIMO_TIPO = 50;
    private static final int TAMANHO_MAXIMO_DESCRICAO = 500;
    private static final int TAMANHO_MAXIMO_CAMINHO = 400;

    private final ArquivoDAO dao;
    private final HistoricoDAO historicoDao;

    public ArquivoController() {
        this.dao = new ArquivoDAOImpl();
        this.historicoDao = new HistoricoDAOImpl();
    }

    /**
     * Cadastra um arquivo novo, vincula as categorias escolhidas e grava um
     * registro de histórico do tipo CRIACAO.
     */
    public Arquivo cadastrar(String nome, String tipo, String descricao, String caminho,
                              boolean favorito, Integer colecaoId, List<Integer> categoriaIds)
            throws ArquivoException {
        validarCampos(nome, tipo, descricao, caminho);

        Arquivo novoArquivo = new Arquivo(nome.trim(), tipo.trim(),
                descricao == null ? "" : descricao.trim(),
                caminho == null ? "" : caminho.trim(),
                favorito, colecaoId);

        try {
            dao.inserir(novoArquivo, categoriaIds);
            historicoDao.inserir(new HistoricoAlteracao(novoArquivo.getId(), "registro", "",
                    "Arquivo cadastrado", TipoOperacao.CRIACAO));
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }

        return novoArquivo;
    }

    /**
     * Compara o arquivo como estava antes da edição com os novos valores
     * digitados, grava um registro de histórico para CADA campo alterado, e
     * só então salva a atualização no banco. dataModificacao é sempre
     * renovada, mesmo que nenhum outro campo tenha mudado (ver §7.3).
     */
    public void editar(Arquivo original, String nome, String tipo, String descricao, String caminho,
                        boolean favorito, Integer colecaoId, List<Integer> categoriaIds)
            throws ArquivoException {
        validarCampos(nome, tipo, descricao, caminho);

        String nomeNovo = nome.trim();
        String tipoNovo = tipo.trim();
        String descricaoNova = descricao == null ? "" : descricao.trim();
        String caminhoNovo = caminho == null ? "" : caminho.trim();

        List<HistoricoAlteracao> mudancas = new ArrayList<>();
        registrarSeMudou(mudancas, original.getId(), "nome", original.getNome(), nomeNovo);
        registrarSeMudou(mudancas, original.getId(), "tipo", original.getTipo(), tipoNovo);
        registrarSeMudou(mudancas, original.getId(), "descrição", original.getDescricao(), descricaoNova);
        registrarSeMudou(mudancas, original.getId(), "caminho", original.getCaminho(), caminhoNovo);
        registrarSeMudou(mudancas, original.getId(), "favorito",
                String.valueOf(original.isFavorito()), String.valueOf(favorito));
        registrarSeMudou(mudancas, original.getId(), "coleção",
                original.getColecaoId() == null ? "" : String.valueOf(original.getColecaoId()),
                colecaoId == null ? "" : String.valueOf(colecaoId));

        original.setNome(nomeNovo);
        original.setTipo(tipoNovo);
        original.setDescricao(descricaoNova);
        original.setCaminho(caminhoNovo);
        original.setFavorito(favorito);
        original.setColecaoId(colecaoId);
        original.setDataModificacao(LocalDateTime.now());

        try {
            dao.atualizar(original);
            sincronizarCategorias(original.getId(), categoriaIds);

            for (HistoricoAlteracao mudanca : mudancas) {
                historicoDao.inserir(mudanca);
            }
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
    }

    private void registrarSeMudou(List<HistoricoAlteracao> mudancas, int arquivoId,
                                   String campo, String valorAnterior, String valorNovo) {
        String anterior = valorAnterior == null ? "" : valorAnterior;
        String novo = valorNovo == null ? "" : valorNovo;
        if (!anterior.equals(novo)) {
            mudancas.add(new HistoricoAlteracao(arquivoId, campo, anterior, novo, TipoOperacao.EDICAO));
        }
    }

    /**
     * Compara a lista de categorias que o arquivo já tinha com a nova
     * seleção do usuário: desvincula quem saiu e vincula quem entrou.
     */
    private void sincronizarCategorias(int arquivoId, List<Integer> categoriaIdsNovos) throws SQLException {
        List<Integer> idsNovos = categoriaIdsNovos == null ? new ArrayList<>() : categoriaIdsNovos;
        List<Categoria> categoriasAtuais = dao.listarCategoriasDoArquivo(arquivoId);

        for (Categoria categoriaAtual : categoriasAtuais) {
            if (!idsNovos.contains(categoriaAtual.getId())) {
                dao.desvincularCategoria(arquivoId, categoriaAtual.getId());
            }
        }

        for (int idNovo : idsNovos) {
            boolean jaTinha = false;
            for (Categoria categoriaAtual : categoriasAtuais) {
                if (categoriaAtual.getId() == idNovo) {
                    jaTinha = true;
                    break;
                }
            }
            if (!jaTinha) {
                dao.vincularCategoria(arquivoId, idNovo);
            }
        }
    }

    public List<Arquivo> listarOrdenado(CriterioOrdenacao criterio, boolean crescente) throws ArquivoException {
        List<Arquivo> lista;
        try {
            lista = dao.listarTodos();
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
        ordenar(lista, criterio, crescente);
        return lista;
    }

    public List<Arquivo> buscar(FiltroBusca filtro, CriterioOrdenacao criterio, boolean crescente)
            throws ArquivoException {
        List<Arquivo> lista;
        try {
            lista = dao.buscar(filtro);
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
        ordenar(lista, criterio, crescente);
        return lista;
    }

    public List<HistoricoAlteracao> buscarHistorico(int arquivoId) throws ArquivoException {
        try {
            return historicoDao.listarPorArquivo(arquivoId);
        } catch (SQLException e) {
            throw traduzirErroBanco(e);
        }
    }

    /**
     * Ordenação fica aqui no Controller (não em ORDER BY no SQL) porque é
     * regra de apresentação, não de acesso a dados — mantém o DAO com
     * responsabilidade única (ver §7.4 da especificação).
     */
    private void ordenar(List<Arquivo> lista, CriterioOrdenacao criterio, boolean crescente) {
        Comparator<Arquivo> comparador = criarComparador(criterio);
        if (!crescente) {
            comparador = comparador.reversed();
        }
        lista.sort(comparador);
    }

    private Comparator<Arquivo> criarComparador(CriterioOrdenacao criterio) {
        switch (criterio) {
            case NOME:
                return Comparator.comparing(Arquivo::getNome, String.CASE_INSENSITIVE_ORDER);
            case TIPO:
                return Comparator.comparing(Arquivo::getTipo, String.CASE_INSENSITIVE_ORDER);
            case DATA:
                return Comparator.comparing(Arquivo::getDataModificacao);
            default:
                // Não deveria acontecer: os três valores do enum estão cobertos acima.
                throw new IllegalArgumentException("Critério de ordenação desconhecido: " + criterio);
        }
    }

    /**
     * Regras de validação de entrada do usuário. Fica no Controller porque
     * é regra de negócio, não responsabilidade da View nem do DAO.
     */
    private void validarCampos(String nome, String tipo, String descricao, String caminho) throws ArquivoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ArquivoException("O nome do arquivo não pode ficar em branco.");
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new ArquivoException("O tipo do arquivo não pode ficar em branco.");
        }
        if (nome.trim().length() > TAMANHO_MAXIMO_NOME) {
            throw new ArquivoException("O nome não pode ter mais que " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        if (tipo.trim().length() > TAMANHO_MAXIMO_TIPO) {
            throw new ArquivoException("O tipo não pode ter mais que " + TAMANHO_MAXIMO_TIPO + " caracteres.");
        }
        if (descricao != null && descricao.trim().length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ArquivoException("A descrição não pode ter mais que " + TAMANHO_MAXIMO_DESCRICAO + " caracteres.");
        }
        if (caminho != null && caminho.trim().length() > TAMANHO_MAXIMO_CAMINHO) {
            throw new ArquivoException("O caminho não pode ter mais que " + TAMANHO_MAXIMO_CAMINHO + " caracteres.");
        }
    }

    /**
     * Converte uma SQLException técnica numa mensagem que faz sentido pra
     * quem está usando o programa. A View nunca vê uma SQLException crua
     * (ver §7.6 da especificação).
     */
    private ArquivoException traduzirErroBanco(SQLException e) {
        // Código de erro do driver MySQL para "não consegui conectar" (ex.: banco fora do ar).
        String estadoSql = e.getSQLState();
        if (estadoSql != null && estadoSql.startsWith("08")) {
            return new ArquivoException(
                    "Não foi possível conectar ao banco de dados. Verifique se o MySQL está em execução.", e);
        }
        // 28000: falha de autenticação (usuário/senha errados) — testado de
        // propósito em §12.4 alterando db.senha no config.properties.
        if ("28000".equals(estadoSql)) {
            return new ArquivoException(
                    "Usuário ou senha do banco de dados estão incorretos. Verifique o arquivo config.properties.", e);
        }
        // 23000: violação de restrição — aqui, o vínculo repetido da mesma
        // categoria no mesmo arquivo (chave primária composta).
        if ("23000".equals(estadoSql)) {
            return new ArquivoException("Essa categoria já está vinculada a este arquivo.", e);
        }
        return new ArquivoException("Ocorreu um erro ao acessar o banco de dados: " + e.getMessage(), e);
    }
}
