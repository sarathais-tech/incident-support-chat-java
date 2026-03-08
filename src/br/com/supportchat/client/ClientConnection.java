package br.com.supportchat.client;

import br.com.supportchat.util.CryptoUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
            return false;
        }
    }

    public String sendAndReceive(String plainMessage) {
        try {
            String encrypted = CryptoUtils.encrypt(plainMessage);
            writer.println(encrypted);

            String response = reader.readLine();
            if (response == null) {
                return "ERRO|Sem resposta do servidor";
            }

            return CryptoUtils.decrypt(response);
        } catch (IOException e) {
            return "ERRO|" + e.getMessage();
        }
    }

    public void disconnect() {
        try {
            if (reader != null) {
                reader.close();
            }
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