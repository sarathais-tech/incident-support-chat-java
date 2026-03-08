package br.com.supportchat.client;

import br.com.supportchat.util.CryptoUtils;

public class ClientGUI {

    public static void main(String[] args) {

        ClientConnection connection = new ClientConnection();

        boolean connected = connection.connect("127.0.0.1", 5000);

        if (connected) {
            System.out.println("Cliente conectado!");

            String mensagem = "ASSUMIR|Carlos|1";

            String encrypted = CryptoUtils.encrypt(mensagem);

            connection.sendMessage(encrypted);

            connection.disconnect();
        } else {
            System.out.println("Falha ao conectar.");
        }
    }
}