package controller;

/**
 * Agrupa os critérios da busca combinada de arquivos. Cada campo nulo (ou
 * false/vazio) significa "não filtrar por este critério" — é o ArquivoDAO
 * quem monta o SQL dinamicamente a partir daqui (ver §7.2 da especificação).
 */
public class FiltroBusca {

    private String texto;
    private String tipo;
    private Integer colecaoId;
    private Integer categoriaId;
    private boolean somenteFavoritos;

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getColecaoId() { return colecaoId; }
    public void setColecaoId(Integer colecaoId) { this.colecaoId = colecaoId; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    public boolean isSomenteFavoritos() { return somenteFavoritos; }
    public void setSomenteFavoritos(boolean somenteFavoritos) { this.somenteFavoritos = somenteFavoritos; }
}
