package controller;

/**
 * Exceção "de negócio" do sistema. A View só conhece esta classe — nunca
 * uma SQLException crua. Isso mantém a camada gráfica desacoplada de
 * detalhes técnicos de banco de dados (ver §7.4 da especificação).
 */
public class ArquivoException extends Exception {

    public ArquivoException(String mensagem) {
        super(mensagem);
    }

    public ArquivoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
