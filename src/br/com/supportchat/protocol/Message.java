package br.com.supportchat.protocol;

public class Message {
    private String type;
    private String username;
    private String role;
    private int ticketId;
    private String categoria;
    private String descricao;
    private String content;

    public Message() {
    }

    public Message(String type, String username, String role, int ticketId, String categoria, String descricao, String content) {
        this.type = type;
        this.username = username;
        this.role = role;
        this.ticketId = ticketId;
        this.categoria = categoria;
        this.descricao = descricao;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getContent() {
        return content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setContent(String content) {
        this.content = content;
    }
}