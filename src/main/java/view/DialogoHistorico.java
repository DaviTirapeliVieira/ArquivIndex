package view;

import controller.ArquivoController;
import controller.ArquivoException;
import model.HistoricoAlteracao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Janela modal com o histórico de alterações de um único arquivo.
 */
public class DialogoHistorico extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public DialogoHistorico(Frame proprietario, ArquivoController controller, int arquivoId, String nomeArquivo) {
        super(proprietario, "Histórico de alterações — " + nomeArquivo, true);

        String[] colunas = {"Data", "Operação", "Campo", "Valor anterior", "Valor novo"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };

        try {
            List<HistoricoAlteracao> historico = controller.buscarHistorico(arquivoId);
            for (HistoricoAlteracao item : historico) {
                modelo.addRow(new Object[]{
                        item.getDataAlteracao().format(FORMATO_DATA),
                        item.getTipoOperacao(),
                        item.getCampoAlterado(),
                        item.getValorAnterior(),
                        item.getValorNovo()
                });
            }
        } catch (ArquivoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

        JTable tabela = new JTable(modelo);

        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(evento -> dispose());
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotao.add(botaoFechar);
        add(painelBotao, BorderLayout.SOUTH);

        setSize(650, 350);
        setLocationRelativeTo(proprietario);
    }
}
