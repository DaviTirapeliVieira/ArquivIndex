package view;

import controller.ArquivoException;
import controller.CategoriaController;
import model.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Aba de categorias (tags): cadastro simples e listagem.
 */
public class PainelCategorias extends JPanel {

    private final CategoriaController controller = new CategoriaController();

    private JTextField campoNome;
    private JTextField campoCor;
    private DefaultTableModel modeloTabela;

    public PainelCategorias() {
        setLayout(new BorderLayout(10, 10));
        add(criarPainelFormulario(), BorderLayout.NORTH);
        add(criarPainelTabela(), BorderLayout.CENTER);
        carregarTabela();
    }

    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel(new GridLayout(2, 2, 5, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Nova categoria"));

        campoNome = new JTextField();
        campoCor = new JTextField();

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(new JLabel("Cor (ex.: #EF4444):"));
        painel.add(campoCor);

        JButton botaoCadastrar = new JButton("Cadastrar categoria");
        botaoCadastrar.addActionListener(evento -> cadastrar());

        JPanel painelExterno = new JPanel(new BorderLayout(5, 5));
        painelExterno.add(painel, BorderLayout.CENTER);
        painelExterno.add(botaoCadastrar, BorderLayout.SOUTH);
        return painelExterno;
    }

    private JPanel criarPainelTabela() {
        String[] colunas = {"ID", "Nome", "Cor"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JTable tabela = new JTable(modeloTabela);

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Categorias cadastradas"));
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    private void cadastrar() {
        try {
            controller.cadastrar(campoNome.getText(), campoCor.getText());
            campoNome.setText("");
            campoCor.setText("");
            carregarTabela();
            JOptionPane.showMessageDialog(this, "Categoria cadastrada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTabela() {
        try {
            List<Categoria> lista = controller.listarTodas();
            modeloTabela.setRowCount(0);
            for (Categoria categoria : lista) {
                modeloTabela.addRow(new Object[]{categoria.getId(), categoria.getNome(), categoria.getCor()});
            }
        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Usado pelo PainelArquivos para preencher os combos/lista de categorias. */
    public List<Categoria> obterTodasCategorias() throws ArquivoException {
        return controller.listarTodas();
    }
}
