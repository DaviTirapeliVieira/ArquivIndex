package view;

import controller.ArquivoException;
import controller.ColecaoController;
import model.Colecao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Aba de coleções: cadastro simples e listagem. Sem edição/exclusão — o
 * escopo da v2 só pediu isso para Arquivo (que precisa de histórico).
 */
public class PainelColecoes extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ColecaoController controller = new ColecaoController();

    private JTextField campoNome;
    private JTextArea campoDescricao;
    private DefaultTableModel modeloTabela;

    public PainelColecoes() {
        setLayout(new BorderLayout(10, 10));
        add(criarPainelFormulario(), BorderLayout.NORTH);
        add(criarPainelTabela(), BorderLayout.CENTER);
        carregarTabela();
    }

    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel(new GridLayout(2, 2, 5, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Nova coleção"));

        campoNome = new JTextField();
        campoDescricao = new JTextArea(2, 20);

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(new JLabel("Descrição:"));
        painel.add(new JScrollPane(campoDescricao));

        JButton botaoCadastrar = new JButton("Cadastrar coleção");
        botaoCadastrar.addActionListener(evento -> cadastrar());

        JPanel painelExterno = new JPanel(new BorderLayout(5, 5));
        painelExterno.add(painel, BorderLayout.CENTER);
        painelExterno.add(botaoCadastrar, BorderLayout.SOUTH);
        return painelExterno;
    }

    private JPanel criarPainelTabela() {
        String[] colunas = {"ID", "Nome", "Descrição", "Criada em"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JTable tabela = new JTable(modeloTabela);

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Coleções cadastradas"));
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    private void cadastrar() {
        try {
            controller.cadastrar(campoNome.getText(), campoDescricao.getText());
            campoNome.setText("");
            campoDescricao.setText("");
            carregarTabela();
            JOptionPane.showMessageDialog(this, "Coleção cadastrada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTabela() {
        try {
            List<Colecao> lista = controller.listarTodas();
            modeloTabela.setRowCount(0);
            for (Colecao colecao : lista) {
                modeloTabela.addRow(new Object[]{
                        colecao.getId(), colecao.getNome(), colecao.getDescricao(),
                        colecao.getDataCriacao().format(FORMATO_DATA)
                });
            }
        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Usado pelo PainelArquivos para preencher os combos de coleção. */
    public List<Colecao> obterTodasColecoes() throws ArquivoException {
        return controller.listarTodas();
    }
}
