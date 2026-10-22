package view;

import controller.ArquivoController;
import controller.ArquivoException;
import controller.CriterioOrdenacao;
import controller.FiltroBusca;
import model.Arquivo;
import model.Categoria;
import model.Colecao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Aba de arquivos: cadastro/edição, listagem, busca combinada, ordenação e
 * acesso ao histórico. Escrita à mão, sem construtor visual, para que cada
 * linha possa ser explicada na apresentação (ver §7.5 da especificação).
 */
public class PainelArquivos extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String ITEM_NENHUMA_COLECAO = "(nenhuma)";
    private static final String ITEM_TODAS_COLECOES = "Todas as coleções";
    private static final String ITEM_TODOS_TIPOS = "Todos os tipos";
    private static final String ITEM_TODAS_CATEGORIAS = "Todas as categorias";

    private final ArquivoController controller = new ArquivoController();
    private final PainelColecoes painelColecoes;
    private final PainelCategorias painelCategorias;

    private List<Colecao> colecoesDisponiveis = new ArrayList<>();
    private List<Categoria> categoriasDisponiveis = new ArrayList<>();

    // Formulário de cadastro/edição.
    private JTextField campoNome;
    private JTextField campoTipo;
    private JTextField campoCaminho;
    private JTextArea campoDescricao;
    private JCheckBox checkFavorito;
    private JComboBox<Object> comboColecaoForm;
    private JList<Categoria> listaCategoriasForm;
    private DefaultListModel<Categoria> modeloListaCategorias;

    // Busca combinada.
    private JTextField campoBuscaTexto;
    private JComboBox<Object> comboBuscaTipo;
    private JComboBox<Object> comboBuscaColecao;
    private JComboBox<Object> comboBuscaCategoria;
    private JCheckBox checkBuscaFavoritos;

    // Ordenação.
    private JComboBox<CriterioOrdenacao> comboCriterio;
    private JRadioButton radioCrescente;
    private JRadioButton radioDecrescente;

    // Tabela e estado de edição.
    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private List<Arquivo> arquivosExibidos = new ArrayList<>();
    private Arquivo arquivoSelecionado;

    // Filtro atualmente aplicado à tabela; null significa "sem filtro, mostrar tudo".
    private FiltroBusca filtroAtivo;

    public PainelArquivos(PainelColecoes painelColecoes, PainelCategorias painelCategorias) {
        this.painelColecoes = painelColecoes;
        this.painelCategorias = painelCategorias;

        setLayout(new BorderLayout(10, 10));
        add(criarPainelFormulario(), BorderLayout.NORTH);
        add(criarPainelTabela(), BorderLayout.CENTER);
        add(criarPainelOrdenacao(), BorderLayout.SOUTH);

        atualizarCombosAuxiliares();
        recarregar();
    }

    // ---------------------------------------------------------------
    // Montagem da tela
    // ---------------------------------------------------------------

    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel(new GridLayout(4, 2, 5, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Cadastrar / editar arquivo"));

        campoNome = new JTextField();
        campoTipo = new JTextField();
        campoCaminho = new JTextField();
        campoDescricao = new JTextArea(3, 20);

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(new JLabel("Tipo:"));
        painel.add(campoTipo);
        painel.add(new JLabel("Caminho:"));
        painel.add(campoCaminho);
        painel.add(new JLabel("Descrição:"));
        painel.add(new JScrollPane(campoDescricao));

        checkFavorito = new JCheckBox("Favorito");
        comboColecaoForm = new JComboBox<>();

        JPanel painelColecaoFavorito = new JPanel(new GridLayout(2, 2, 5, 5));
        painelColecaoFavorito.add(new JLabel("Coleção:"));
        painelColecaoFavorito.add(comboColecaoForm);
        painelColecaoFavorito.add(new JLabel(""));
        painelColecaoFavorito.add(checkFavorito);

        modeloListaCategorias = new DefaultListModel<>();
        listaCategoriasForm = new JList<>(modeloListaCategorias);
        listaCategoriasForm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaCategoriasForm.setVisibleRowCount(3);

        JPanel painelCategoriasForm = new JPanel(new BorderLayout());
        painelCategoriasForm.add(new JLabel("Categorias:"), BorderLayout.NORTH);
        painelCategoriasForm.add(new JScrollPane(listaCategoriasForm), BorderLayout.CENTER);

        JPanel painelDireita = new JPanel(new GridLayout(2, 1, 5, 5));
        painelDireita.add(painelColecaoFavorito);
        painelDireita.add(painelCategoriasForm);

        JButton botaoCadastrar = new JButton("Cadastrar");
        botaoCadastrar.addActionListener(evento -> cadastrar());

        JButton botaoSalvarEdicao = new JButton("Salvar edição");
        botaoSalvarEdicao.addActionListener(evento -> salvarEdicao());

        JButton botaoVerHistorico = new JButton("Ver histórico");
        botaoVerHistorico.addActionListener(evento -> verHistorico());

        JButton botaoLimpar = new JButton("Limpar formulário");
        botaoLimpar.addActionListener(evento -> limparFormulario());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoSalvarEdicao);
        painelBotoes.add(botaoVerHistorico);
        painelBotoes.add(botaoLimpar);

        JPanel painelSuperior = new JPanel(new BorderLayout(5, 5));
        painelSuperior.add(painel, BorderLayout.CENTER);
        painelSuperior.add(painelDireita, BorderLayout.EAST);

        JPanel painelExterno = new JPanel(new BorderLayout(5, 5));
        painelExterno.add(painelSuperior, BorderLayout.CENTER);
        painelExterno.add(painelBotoes, BorderLayout.SOUTH);
        return painelExterno;
    }

    private JPanel criarPainelBusca() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painel.setBorder(BorderFactory.createTitledBorder("Busca combinada"));

        campoBuscaTexto = new JTextField(12);
        comboBuscaTipo = new JComboBox<>();
        comboBuscaColecao = new JComboBox<>();
        comboBuscaCategoria = new JComboBox<>();
        checkBuscaFavoritos = new JCheckBox("Somente favoritos");

        JButton botaoBuscar = new JButton("Buscar");
        botaoBuscar.addActionListener(evento -> buscar());

        JButton botaoLimparFiltros = new JButton("Limpar filtros");
        botaoLimparFiltros.addActionListener(evento -> limparFiltros());

        painel.add(new JLabel("Texto:"));
        painel.add(campoBuscaTexto);
        painel.add(new JLabel("Tipo:"));
        painel.add(comboBuscaTipo);
        painel.add(new JLabel("Coleção:"));
        painel.add(comboBuscaColecao);
        painel.add(new JLabel("Categoria:"));
        painel.add(comboBuscaCategoria);
        painel.add(checkBuscaFavoritos);
        painel.add(botaoBuscar);
        painel.add(botaoLimparFiltros);

        return painel;
    }

    private JPanel criarPainelTabela() {
        String[] colunas = {"ID", "Nome", "Tipo", "Coleção", "Categorias", "Favorito", "Criado em", "Modificado em"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modeloTabela);
        tabela.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                selecionarLinha();
            }
        });

        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Arquivos cadastrados"));
        painel.add(criarPainelBusca(), BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelOrdenacao() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painel.setBorder(BorderFactory.createTitledBorder("Ordenação"));

        painel.add(new JLabel("Ordenar por:"));
        comboCriterio = new JComboBox<>(CriterioOrdenacao.values());
        comboCriterio.addActionListener(evento -> recarregar());
        painel.add(comboCriterio);

        radioCrescente = new JRadioButton("Crescente", true);
        radioDecrescente = new JRadioButton("Decrescente");
        ButtonGroup grupoOrdem = new ButtonGroup();
        grupoOrdem.add(radioCrescente);
        grupoOrdem.add(radioDecrescente);
        radioCrescente.addActionListener(evento -> recarregar());
        radioDecrescente.addActionListener(evento -> recarregar());

        painel.add(radioCrescente);
        painel.add(radioDecrescente);
        return painel;
    }

    // ---------------------------------------------------------------
    // Combos auxiliares (coleção/categoria), preenchidos a partir das
    // outras abas — chamado ao criar a tela e sempre que o usuário volta
    // para a aba Arquivos (ver JanelaPrincipal), para refletir cadastros
    // feitos nas outras abas.
    // ---------------------------------------------------------------

    public void atualizarCombosAuxiliares() {
        try {
            colecoesDisponiveis = painelColecoes.obterTodasColecoes();
            categoriasDisponiveis = painelCategorias.obterTodasCategorias();

            comboColecaoForm.removeAllItems();
            comboColecaoForm.addItem(ITEM_NENHUMA_COLECAO);
            for (Colecao colecao : colecoesDisponiveis) {
                comboColecaoForm.addItem(colecao);
            }

            modeloListaCategorias.clear();
            for (Categoria categoria : categoriasDisponiveis) {
                modeloListaCategorias.addElement(categoria);
            }

            comboBuscaColecao.removeAllItems();
            comboBuscaColecao.addItem(ITEM_TODAS_COLECOES);
            for (Colecao colecao : colecoesDisponiveis) {
                comboBuscaColecao.addItem(colecao);
            }

            comboBuscaCategoria.removeAllItems();
            comboBuscaCategoria.addItem(ITEM_TODAS_CATEGORIAS);
            for (Categoria categoria : categoriasDisponiveis) {
                comboBuscaCategoria.addItem(categoria);
            }

        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------
    // Cadastro / edição / histórico
    // ---------------------------------------------------------------

    private void cadastrar() {
        try {
            Integer colecaoId = obterColecaoSelecionadaForm();
            List<Integer> categoriaIds = obterCategoriasSelecionadasForm();

            controller.cadastrar(campoNome.getText(), campoTipo.getText(), campoDescricao.getText(),
                    campoCaminho.getText(), checkFavorito.isSelected(), colecaoId, categoriaIds);

            limparFormulario();
            recarregar();

            JOptionPane.showMessageDialog(this, "Arquivo cadastrado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarEdicao() {
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um arquivo na tabela antes de salvar a edição.",
                    "Nenhum arquivo selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer colecaoId = obterColecaoSelecionadaForm();
            List<Integer> categoriaIds = obterCategoriasSelecionadasForm();

            controller.editar(arquivoSelecionado, campoNome.getText(), campoTipo.getText(),
                    campoDescricao.getText(), campoCaminho.getText(), checkFavorito.isSelected(),
                    colecaoId, categoriaIds);

            limparFormulario();
            recarregar();

            JOptionPane.showMessageDialog(this, "Arquivo atualizado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verHistorico() {
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um arquivo na tabela antes de ver o histórico.",
                    "Nenhum arquivo selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window janela = SwingUtilities.getWindowAncestor(this);
        Frame proprietario = (janela instanceof Frame) ? (Frame) janela : null;
        DialogoHistorico dialogo = new DialogoHistorico(proprietario, controller,
                arquivoSelecionado.getId(), arquivoSelecionado.getNome());
        dialogo.setVisible(true);
    }

    private Integer obterColecaoSelecionadaForm() {
        Object selecionado = comboColecaoForm.getSelectedItem();
        if (selecionado instanceof Colecao) {
            return ((Colecao) selecionado).getId();
        }
        return null;
    }

    private List<Integer> obterCategoriasSelecionadasForm() {
        List<Integer> ids = new ArrayList<>();
        for (Categoria categoria : listaCategoriasForm.getSelectedValuesList()) {
            ids.add(categoria.getId());
        }
        return ids;
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoTipo.setText("");
        campoCaminho.setText("");
        campoDescricao.setText("");
        checkFavorito.setSelected(false);
        comboColecaoForm.setSelectedItem(ITEM_NENHUMA_COLECAO);
        listaCategoriasForm.clearSelection();
        tabela.clearSelection();
        arquivoSelecionado = null;
    }

    // ---------------------------------------------------------------
    // Seleção na tabela: preenche o formulário com o arquivo escolhido,
    // habilitando a edição.
    // ---------------------------------------------------------------

    private void selecionarLinha() {
        int linha = tabela.getSelectedRow();
        if (linha < 0 || linha >= arquivosExibidos.size()) {
            arquivoSelecionado = null;
            return;
        }

        arquivoSelecionado = arquivosExibidos.get(linha);

        campoNome.setText(arquivoSelecionado.getNome());
        campoTipo.setText(arquivoSelecionado.getTipo());
        campoCaminho.setText(arquivoSelecionado.getCaminho());
        campoDescricao.setText(arquivoSelecionado.getDescricao());
        checkFavorito.setSelected(arquivoSelecionado.isFavorito());

        if (arquivoSelecionado.getColecaoId() == null) {
            comboColecaoForm.setSelectedItem(ITEM_NENHUMA_COLECAO);
        } else {
            for (Colecao colecao : colecoesDisponiveis) {
                if (colecao.getId() == arquivoSelecionado.getColecaoId()) {
                    comboColecaoForm.setSelectedItem(colecao);
                    break;
                }
            }
        }

        listaCategoriasForm.clearSelection();
        for (Categoria categoriaDoArquivo : arquivoSelecionado.getCategorias()) {
            for (int i = 0; i < modeloListaCategorias.size(); i++) {
                if (modeloListaCategorias.get(i).getId() == categoriaDoArquivo.getId()) {
                    listaCategoriasForm.addSelectionInterval(i, i);
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Busca combinada e listagem
    // ---------------------------------------------------------------

    private void buscar() {
        FiltroBusca filtro = new FiltroBusca();

        String texto = campoBuscaTexto.getText();
        if (texto != null && !texto.isBlank()) {
            filtro.setTexto(texto);
        }

        Object tipoSelecionado = comboBuscaTipo.getSelectedItem();
        if (tipoSelecionado != null && !ITEM_TODOS_TIPOS.equals(tipoSelecionado)) {
            filtro.setTipo((String) tipoSelecionado);
        }

        Object colecaoSelecionada = comboBuscaColecao.getSelectedItem();
        if (colecaoSelecionada instanceof Colecao) {
            filtro.setColecaoId(((Colecao) colecaoSelecionada).getId());
        }

        Object categoriaSelecionada = comboBuscaCategoria.getSelectedItem();
        if (categoriaSelecionada instanceof Categoria) {
            filtro.setCategoriaId(((Categoria) categoriaSelecionada).getId());
        }

        filtro.setSomenteFavoritos(checkBuscaFavoritos.isSelected());

        filtroAtivo = filtro;
        recarregar();
    }

    private void limparFiltros() {
        campoBuscaTexto.setText("");
        comboBuscaTipo.setSelectedIndex(0);
        comboBuscaColecao.setSelectedIndex(0);
        comboBuscaCategoria.setSelectedIndex(0);
        checkBuscaFavoritos.setSelected(false);
        filtroAtivo = null;
        recarregar();
    }

    /**
     * Busca a lista atualizada (com o filtro ativo, se houver, já ordenada
     * pelo Controller) e repovoa a tabela do zero.
     */
    private void recarregar() {
        try {
            CriterioOrdenacao criterio = (CriterioOrdenacao) comboCriterio.getSelectedItem();
            boolean crescente = radioCrescente.isSelected();

            List<Arquivo> lista = (filtroAtivo == null)
                    ? controller.listarOrdenado(criterio, crescente)
                    : controller.buscar(filtroAtivo, criterio, crescente);

            arquivosExibidos = lista;
            preencherTabela(lista);
            atualizarComboTipoBusca();

        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherTabela(List<Arquivo> lista) {
        modeloTabela.setRowCount(0);
        for (Arquivo arquivo : lista) {
            String nomeColecao = arquivo.getColecaoId() == null ? "" : nomeColecaoPorId(arquivo.getColecaoId());

            StringBuilder categorias = new StringBuilder();
            for (Categoria categoria : arquivo.getCategorias()) {
                if (categorias.length() > 0) {
                    categorias.append(", ");
                }
                categorias.append(categoria.getNome());
            }

            modeloTabela.addRow(new Object[]{
                    arquivo.getId(),
                    arquivo.getNome(),
                    arquivo.getTipo(),
                    nomeColecao,
                    categorias.toString(),
                    arquivo.isFavorito() ? "Sim" : "Não",
                    arquivo.getDataCriacao().format(FORMATO_DATA),
                    arquivo.getDataModificacao().format(FORMATO_DATA)
            });
        }
    }

    private String nomeColecaoPorId(int colecaoId) {
        for (Colecao colecao : colecoesDisponiveis) {
            if (colecao.getId() == colecaoId) {
                return colecao.getNome();
            }
        }
        return "";
    }

    // O combo de tipos da busca é montado a partir dos tipos que já
    // aparecem na listagem atual — mais simples que manter um cadastro de
    // tipos à parte, que a especificação não pede.
    private void atualizarComboTipoBusca() {
        Object selecionadoAntes = comboBuscaTipo.getSelectedItem();

        comboBuscaTipo.removeAllItems();
        comboBuscaTipo.addItem(ITEM_TODOS_TIPOS);
        for (String tipo : tiposDistintos()) {
            comboBuscaTipo.addItem(tipo);
        }

        if (selecionadoAntes != null) {
            comboBuscaTipo.setSelectedItem(selecionadoAntes);
        }
    }

    private List<String> tiposDistintos() {
        List<String> tipos = new ArrayList<>();
        for (Arquivo arquivo : arquivosExibidos) {
            if (!tipos.contains(arquivo.getTipo())) {
                tipos.add(arquivo.getTipo());
            }
        }
        return tipos;
    }
}
