import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import view.JanelaPrincipal;

/**
 * Ponto de entrada do programa. A interface gráfica é criada dentro de
 * SwingUtilities.invokeLater porque componentes Swing devem ser criados e
 * manipulados apenas pela thread de eventos do Swing (Event Dispatch
 * Thread), nunca pela thread main diretamente.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JanelaPrincipal janela = new JanelaPrincipal();
                janela.setVisible(true);
            } catch (Exception e) {
                // Erro de configuração (ex.: config.properties ausente ou
                // incompleto) acontece antes de qualquer tela existir — sem
                // isto, o programa fecharia em silêncio.
                JOptionPane.showMessageDialog(null,
                        "Não foi possível iniciar o ArquivIndex:\n" + e.getMessage(),
                        "Erro ao iniciar", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
