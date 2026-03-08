package br.com.supportchat.server;

import br.com.supportchat.model.Ticket;
import br.com.supportchat.util.CryptoUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final TicketManager ticketManager;

    public ClientHandler(Socket socket, TicketManager ticketManager) {
        this.socket = socket;
        this.ticketManager = ticketManager;
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

                processMessage(decrypted);
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado.");
        }
    }

    private void processMessage(String decrypted) {
        if (decrypted.startsWith("TICKET|")) {
            String[] parts = decrypted.split("\\|", 4);

            if (parts.length == 4) {
                String clienteNome = parts[1];
                String categoria = parts[2];
                String descricao = parts[3];

                Ticket ticket = ticketManager.createTicket(clienteNome, categoria, descricao);

                System.out.println("=== TICKET CRIADO ===");
                System.out.println("ID: " + ticket.getId());
                System.out.println("Cliente: " + ticket.getClienteNome());
                System.out.println("Categoria: " + ticket.getCategoria());
                System.out.println("Descrição: " + ticket.getDescricao());
                System.out.println("Status: " + ticket.getStatus());
                System.out.println("=====================");
            } else {
                System.out.println("Formato de ticket inválido.");
            }
        } else {
            System.out.println("Mensagem comum recebida: " + decrypted);
        }
    }
}