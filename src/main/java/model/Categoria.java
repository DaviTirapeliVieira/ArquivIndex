package model;

/**
 * Etiqueta (tag) aplicável a vários arquivos — relacionamento N:N com
 * Arquivo, feito através da tabela de junção arquivo_categoria.
 */
public class Categoria {

    private int id;
    private final String nome;
    private final String cor;

    public Categoria(String nome, String cor) {
        this.id = 0;
        this.nome = nome;
        this.cor = cor;
    }

    public Categoria(int id, String nome, String cor) {
        this.id = id;
        this.nome = nome;
        this.cor = cor;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCor() { return cor; }

    public void setId(int id) {
        this.id = id;
    }

    // toString() retorna só o nome: este objeto é exibido direto em JList e
    // JComboBox, que desenham cada item chamando toString().
    @Override
    public String toString() {
        return nome;
    }
}
