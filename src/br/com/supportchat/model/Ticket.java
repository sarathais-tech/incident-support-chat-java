package br.com.supportchat.model;

public class Ticket {
    private int id;
    private String clienteNome;
    private String categoria;
    private String descricao;
    private String status;
    private String tecnicoNome;

    public Ticket(int id, String clienteNome, String categoria, String descricao) {
        this.id = id;
        this.clienteNome = clienteNome;
        this.categoria = categoria;
        this.descricao = descricao;
        this.status = "ABERTO";
        this.tecnicoNome = null;
    }

    public int getId() {
        return id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getStatus() {
        return status;
    }

    public String getTecnicoNome() {
        return tecnicoNome;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTecnicoNome(String tecnicoNome) {
        this.tecnicoNome = tecnicoNome;
    }

    @Override
    public String toString() {
        return "Ticket #" + id +
                " | Cliente: " + clienteNome +
                " | Categoria: " + categoria +
                " | Status: " + status +
                (tecnicoNome != null ? " | Técnico: " + tecnicoNome : "");
    }
}