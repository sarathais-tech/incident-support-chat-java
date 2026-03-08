package br.com.supportchat.client;

public class ClientGUI {

    public static void main(String[] args) {
        ClientConnection connection = new ClientConnection();

        boolean connected = connection.connect("127.0.0.1", 5000);

        if (connected) {
            System.out.println("Cliente conectado com sucesso!");
            connection.sendMessage("Olá servidor, aqui é o cliente!");
            connection.disconnect();
        } else {
            System.out.println("Não foi possível conectar.");
        }
    }
}