package model;

import java.time.LocalDateTime;

/**
 * Pasta lógica que agrupa arquivos (relacionamento 1:N com Arquivo).
 */
public class Colecao {

    private int id;
    private final String nome;
    private final String descricao;
    private final LocalDateTime dataCriacao;

    // Construtor para uma coleção NOVA, ainda não salva no banco.
    public Colecao(String nome, String descricao) {
        this.id = 0;
        this.nome = nome;
        this.descricao = descricao;
        this.dataCriacao = LocalDateTime.now();
    }

    // Construtor para RECONSTRUIR uma coleção já existente no banco.
    public Colecao(int id, String nome, String descricao, LocalDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataCriacao = dataCriacao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public void setId(int id) {
        this.id = id;
    }

    // toString() retorna só o nome porque este objeto é exibido direto em
    // JComboBox — o combo chama toString() de cada item para desenhar a lista.
    @Override
    public String toString() {
        return nome;
    }
}
