package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Arquivo {

    private int id; // não é 'final': o banco atribui o valor real após o INSERT

    // Campos editáveis: a v2 trouxe edição de volta ao escopo (para viabilizar
    // o histórico de alterações), então estes campos não são mais 'final'.
    private String nome;
    private String tipo;
    private String descricao;
    private String caminho;
    private boolean favorito;
    private Integer colecaoId; // null = arquivo sem coleção associada

    private final LocalDateTime dataCriacao; // nunca muda depois de criado
    private LocalDateTime dataModificacao;   // atualizada a cada edição

    // Categorias vinculadas a este arquivo (relação N:N). Não é persistida
    // diretamente por este objeto: o DAO preenche esta lista depois de buscar
    // os vínculos na tabela arquivo_categoria.
    private List<Categoria> categorias = new ArrayList<>();

    // Construtor para um arquivo NOVO, ainda não salvo no banco.
    // O id começa em 0, significando "ainda sem identidade oficial".
    public Arquivo(String nome, String tipo, String descricao, String caminho,
                    boolean favorito, Integer colecaoId) {
        this.id = 0;
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.caminho = caminho;
        this.favorito = favorito;
        this.colecaoId = colecaoId;
        this.dataCriacao = LocalDateTime.now();
        this.dataModificacao = this.dataCriacao;
    }

    // Construtor para RECONSTRUIR um arquivo que já existe no banco
    // (usado pelo DAO ao ler uma linha da tabela e montar o objeto de volta).
    public Arquivo(int id, String nome, String tipo, String descricao, String caminho, boolean favorito,
                    LocalDateTime dataCriacao, LocalDateTime dataModificacao, Integer colecaoId) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.caminho = caminho;
        this.favorito = favorito;
        this.dataCriacao = dataCriacao;
        this.dataModificacao = dataModificacao;
        this.colecaoId = colecaoId;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public String getCaminho() { return caminho; }
    public boolean isFavorito() { return favorito; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataModificacao() { return dataModificacao; }
    public Integer getColecaoId() { return colecaoId; }
    public List<Categoria> getCategorias() { return new ArrayList<>(categorias);}

    // Chamado pelo DAO logo após o INSERT, com o ID que o MySQL gerou.
    public void setId(int id) { this.id = id; }

    // Os setters abaixo só existem porque a v2 permite edição. O Controller é
    // quem decide quando chamá-los, sempre depois de validar e comparar com
    // os valores antigos para gerar o histórico (ver ArquivoController).
    public void setNome(String nome) { this.nome = nome; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setCaminho(String caminho) { this.caminho = caminho; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
    public void setColecaoId(Integer colecaoId) { this.colecaoId = colecaoId; }
    public void setDataModificacao(LocalDateTime dataModificacao) { this.dataModificacao = dataModificacao; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias == null ? new ArrayList<>() : new ArrayList<>(categorias); }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[ID %02d] %-20s | Tipo: %-10s | Modificado em: %s%n           Descrição: %s",
                id, nome, tipo, dataModificacao.format(fmt), descricao);
    }
}
