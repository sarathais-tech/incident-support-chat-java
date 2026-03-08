package br.com.supportchat.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {

    private Socket socket;
    private PrintWriter writer;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public void disconnect() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao desconectar: " + e.getMessage());
        }
    }
}