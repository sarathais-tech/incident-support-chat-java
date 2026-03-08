package br.com.supportchat.server;

import br.com.supportchat.util.CryptoUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;

            while ((message = reader.readLine()) != null) {

                String decrypted = CryptoUtils.decrypt(message);

                System.out.println("Mensagem recebida (criptografada): " + message);
                System.out.println("Mensagem descriptografada: " + decrypted);

            }

        } catch (IOException e) {

            System.out.println("Cliente desconectado.");

        }
    }
}