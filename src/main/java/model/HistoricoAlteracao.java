package model;

import java.time.LocalDateTime;

/**
 * Um registro da trilha de auditoria de um arquivo: o que mudou, de que
 * valor para que valor, em que operação e quando.
 */
public class HistoricoAlteracao {

    private int id;
    private final int arquivoId;
    private final String campoAlterado;
    private final String valorAnterior;
    private final String valorNovo;
    private final TipoOperacao tipoOperacao;
    private final LocalDateTime dataAlteracao;

    // Construtor para um registro NOVO, gerado agora pelo Controller.
    public HistoricoAlteracao(int arquivoId, String campoAlterado, String valorAnterior,
                               String valorNovo, TipoOperacao tipoOperacao) {
        this.id = 0;
        this.arquivoId = arquivoId;
        this.campoAlterado = campoAlterado;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.tipoOperacao = tipoOperacao;
        this.dataAlteracao = LocalDateTime.now();
    }

    // Construtor para RECONSTRUIR um registro já existente no banco.
    public HistoricoAlteracao(int id, int arquivoId, String campoAlterado, String valorAnterior,
                               String valorNovo, TipoOperacao tipoOperacao, LocalDateTime dataAlteracao) {
        this.id = id;
        this.arquivoId = arquivoId;
        this.campoAlterado = campoAlterado;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.tipoOperacao = tipoOperacao;
        this.dataAlteracao = dataAlteracao;
    }

    public int getId() { return id; }
    public int getArquivoId() { return arquivoId; }
    public String getCampoAlterado() { return campoAlterado; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNovo() { return valorNovo; }
    public TipoOperacao getTipoOperacao() { return tipoOperacao; }
    public LocalDateTime getDataAlteracao() { return dataAlteracao; }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: '%s' -> '%s'", tipoOperacao, campoAlterado, valorAnterior, valorNovo);
    }
}
