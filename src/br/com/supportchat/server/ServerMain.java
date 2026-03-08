package br.com.supportchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    private static final int PORT = 5000;

    public static void main(String[] args) {

        System.out.println("Servidor iniciado...");
        System.out.println("Aguardando conexões na porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);

                new Thread(handler).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}