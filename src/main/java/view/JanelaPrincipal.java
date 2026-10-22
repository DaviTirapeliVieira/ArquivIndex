package view;

import javax.swing.*;

/**
 * Janela única do sistema: três abas (Arquivos, Coleções, Categorias).
 * Escrita à mão, sem construtor visual (ver §7.5 da especificação).
 */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("ArquivIndex — Catálogo de Arquivos");

        PainelColecoes painelColecoes = new PainelColecoes();
        PainelCategorias painelCategorias = new PainelCategorias();
        PainelArquivos painelArquivos = new PainelArquivos(painelColecoes, painelCategorias);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Arquivos", painelArquivos);
        abas.addTab("Coleções", painelColecoes);
        abas.addTab("Categorias", painelCategorias);

        // Sempre que o usuário volta para a aba Arquivos, atualiza os
        // combos de coleção/categoria — assim um cadastro feito nas outras
        // abas aparece disponível sem precisar reiniciar o programa.
        abas.addChangeListener(evento -> {
            if (abas.getSelectedComponent() == painelArquivos) {
                painelArquivos.atualizarCombosAuxiliares();
            }
        });

        add(abas);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null); // centraliza a janela na tela
    }
}
