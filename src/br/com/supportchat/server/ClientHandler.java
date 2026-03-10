package br.com.supportchat.server;

import br.com.supportchat.model.Ticket;
import br.com.supportchat.util.CryptoUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private static final Map<Integer, PrintWriter> clientTicketWriters = new ConcurrentHashMap<>();
    private static final Map<Integer, PrintWriter> technicianTicketWriters = new ConcurrentHashMap<>();
    private static final Map<String, PrintWriter> technicianGlobalWriters = new ConcurrentHashMap<>();

    private final Socket socket;
    private final TicketManager ticketManager;

    public ClientHandler(Socket socket, TicketManager ticketManager) {
        this.socket = socket;
        this.ticketManager = ticketManager;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;

            while ((message = reader.readLine()) != null) {
                String decrypted = CryptoUtils.decrypt(message);

                System.out.println("Mensagem recebida (criptografada): " + message);
                System.out.println("Mensagem descriptografada: " + decrypted);

                String response = processMessage(decrypted, writer);
                if (response != null) {
                    writer.println(CryptoUtils.encrypt(response));
                }
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado.");
        }
    }

    private String processMessage(String decrypted, PrintWriter writer) {
        if (decrypted.startsWith("TICKET|")) {
            return processTicketCreation(decrypted, writer);
        } else if (decrypted.startsWith("LISTAR|")) {
            return processListTickets();
        } else if (decrypted.startsWith("ASSUMIR|")) {
            return processAssignTicket(decrypted, writer);
        } else if (decrypted.startsWith("REGISTRAR_CLIENTE|")) {
            return processRegisterClient(decrypted, writer);
        } else if (decrypted.startsWith("REGISTRAR_TECNICO|")) {
            return processRegisterTechnician(decrypted, writer);
        } else if (decrypted.startsWith("TEC_GLOBAL|")) {
            return processTechnicianGlobalMessage(decrypted);
        } else if (decrypted.startsWith("CHAT|")) {
            return processChatMessage(decrypted);
        } else if (decrypted.startsWith("RESOLVER|")) {
            return processResolveTicket(decrypted);
        } else {
            System.out.println("Mensagem comum recebida: " + decrypted);
            return "OK|Mensagem recebida";
        }
    }

    private String processTicketCreation(String decrypted, PrintWriter writer) {
        String[] parts = decrypted.split("\\|", 4);

        if (parts.length == 4) {
            String clienteNome = parts[1];
            String categoria = parts[2];
            String descricao = parts[3];

            Ticket ticket = ticketManager.createTicket(clienteNome, categoria, descricao);
            clientTicketWriters.put(ticket.getId(), writer);

            System.out.println("=== TICKET CRIADO ===");
            System.out.println("ID: " + ticket.getId());
            System.out.println("Cliente: " + ticket.getClienteNome());
            System.out.println("Categoria: " + ticket.getCategoria());
            System.out.println("Descrição: " + ticket.getDescricao());
            System.out.println("Status: " + ticket.getStatus());
            System.out.println("=====================");

            notifyTechniciansNewTicket(ticket);

            return "OK|Ticket criado com ID " + ticket.getId();
        }

        return "ERRO|Formato de ticket inválido";
    }

    private String processListTickets() {
        List<Ticket> tickets = ticketManager.listTickets();

        System.out.println("=== LISTA DE TICKETS ===");

        if (tickets.isEmpty()) {
            System.out.println("Nenhum ticket cadastrado.");
            System.out.println("========================");
            return "LISTA|Nenhum ticket cadastrado.";
        }

        StringBuilder builder = new StringBuilder("LISTA|");

        for (Ticket ticket : tickets) {
            System.out.println(ticket);

            builder.append(ticket.getId()).append(";")
                    .append(ticket.getClienteNome()).append(";")
                    .append(ticket.getCategoria()).append(";")
                    .append(ticket.getStatus()).append(";")
                    .append(ticket.getTecnicoNome() == null ? "-" : ticket.getTecnicoNome())
                    .append("#");
        }

        System.out.println("========================");
        return builder.toString();
    }

    private String processAssignTicket(String decrypted, PrintWriter writer) {
        String[] parts = decrypted.split("\\|", 3);

        if (parts.length == 3) {
            String tecnicoNome = parts[1];
            int ticketId;

            try {
                ticketId = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return "ERRO|ID do ticket inválido";
            }

            boolean assigned = ticketManager.assignTechnician(ticketId, tecnicoNome);

            if (assigned) {
                Ticket ticket = ticketManager.findById(ticketId);
                technicianTicketWriters.put(ticketId, writer);

                System.out.println("=== TICKET EM ATENDIMENTO ===");
                System.out.println("Ticket #" + ticket.getId() + " assumido por " + tecnicoNome);
                System.out.println("Status: " + ticket.getStatus());
                System.out.println("=============================");

                PrintWriter clientWriter = clientTicketWriters.get(ticketId);
                if (clientWriter != null) {
                    clientWriter.println(CryptoUtils.encrypt(
                            "CHAT|Sistema|Ticket " + ticketId + " agora está em atendimento por " + tecnicoNome
                    ));
                }

                notifyTechniciansGlobal(
                        "TEC_GLOBAL|Sistema|Ticket " + ticketId + " foi assumido por " + tecnicoNome
                );

                return "OK|Ticket " + ticketId + " assumido com sucesso";
            }

            return "ERRO|Não foi possível assumir o ticket " + ticketId;
        }

        return "ERRO|Formato de atribuição inválido";
    }

    private String processRegisterClient(String decrypted, PrintWriter writer) {
        String[] parts = decrypted.split("\\|", 3);

        if (parts.length != 3) {
            return "ERRO|Formato de registro de cliente inválido";
        }

        String clienteNome = parts[1];
        int ticketId;

        try {
            ticketId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "ERRO|ID do ticket inválido";
        }

        Ticket ticket = ticketManager.findById(ticketId);
        if (ticket == null) {
            return "ERRO|Ticket não encontrado";
        }

        if (!ticket.getClienteNome().equals(clienteNome)) {
            return "ERRO|Cliente não autorizado para esse ticket";
        }

        clientTicketWriters.put(ticketId, writer);

        System.out.println("[CHAT] Cliente " + clienteNome + " registrado no ticket " + ticketId);

        return "OK|Cliente registrado no chat do ticket " + ticketId;
    }

    private String processRegisterTechnician(String decrypted, PrintWriter writer) {
        String[] parts = decrypted.split("\\|", 2);

        if (parts.length != 2) {
            return "ERRO|Formato de registro de técnico inválido";
        }

        String tecnicoNome = parts[1].trim();

        if (tecnicoNome.isEmpty()) {
            return "ERRO|Nome do técnico inválido";
        }

        technicianGlobalWriters.put(tecnicoNome, writer);

        System.out.println("[TECNICO] " + tecnicoNome + " registrado no chat global.");

        return "OK|Técnico registrado no chat global";
    }

    private String processTechnicianGlobalMessage(String decrypted) {
        String[] parts = decrypted.split("\\|", 3);

        if (parts.length != 3) {
            return "ERRO|Formato de mensagem global inválido";
        }

        String tecnicoNome = parts[1];
        String chatMessage = parts[2];

        String formattedMessage = "TEC_GLOBAL|" + tecnicoNome + "|" + chatMessage;

        notifyTechniciansGlobal(formattedMessage);

        System.out.println("[CHAT TECNICOS] " + tecnicoNome + ": " + chatMessage);

        return "OK|Mensagem global enviada";
    }

    private String processResolveTicket(String decrypted) {

        String[] parts = decrypted.split("\\|", 3);

        if (parts.length != 3) {
            return "ERRO|Formato de resolução inválido";
        }

        String tecnicoNome = parts[1];
        int ticketId;

        try {
            ticketId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "ERRO|ID do ticket inválido";
        }

        boolean resolved = ticketManager.resolveTicket(ticketId, tecnicoNome);

        if (!resolved) {
            return "ERRO|Não foi possível resolver o ticket";
        }

        System.out.println("=== TICKET RESOLVIDO ===");
        System.out.println("Ticket #" + ticketId + " resolvido por " + tecnicoNome);
        System.out.println("========================");

        PrintWriter clientWriter = clientTicketWriters.get(ticketId);

        if (clientWriter != null) {
            clientWriter.println(CryptoUtils.encrypt(
                "CHAT|Sistema|Seu ticket #" + ticketId + " foi resolvido por " + tecnicoNome
            ));
        }
    

        notifyTechniciansGlobal(
                "TEC_GLOBAL|Sistema|Ticket " + ticketId + " foi resolvido por " + tecnicoNome
        );

        return "OK|Ticket resolvido";
    }

    private String processChatMessage(String decrypted) {
        String[] parts = decrypted.split("\\|", 4);

        if (parts.length != 4) {
            return "ERRO|Formato de chat inválido";
        }

        String sender = parts[1];
        int ticketId;
        String chatMessage = parts[3];

        try {
            ticketId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "ERRO|ID do ticket inválido";
        }

        Ticket ticket = ticketManager.findById(ticketId);
        if (ticket == null) {
            return "ERRO|Ticket não encontrado";
        }

        String responseMessage = "CHAT|" + sender + "|" + chatMessage;

        PrintWriter clientWriter = clientTicketWriters.get(ticketId);
        PrintWriter technicianWriter = technicianTicketWriters.get(ticketId);

        if (sender.equals(ticket.getClienteNome())) {
            if (technicianWriter != null) {
                technicianWriter.println(CryptoUtils.encrypt(responseMessage));
            }
            System.out.println("[CHAT] Ticket " + ticketId + " | Cliente " + sender + ": " + chatMessage);
            return "OK|Mensagem enviada";
        }

        if (ticket.getTecnicoNome() != null && sender.equals(ticket.getTecnicoNome())) {
            if (clientWriter != null) {
                clientWriter.println(CryptoUtils.encrypt(responseMessage));
            }
            System.out.println("[CHAT] Ticket " + ticketId + " | Técnico " + sender + ": " + chatMessage);
            return "OK|Mensagem enviada";
        }

        return "ERRO|Usuário não autorizado para esse ticket";
    }

    private void notifyTechniciansNewTicket(Ticket ticket) {
        String message = "TEC_GLOBAL|Sistema|Novo ticket #" + ticket.getId()
                + " aberto por " + ticket.getClienteNome()
                + " | Categoria: " + ticket.getCategoria();

        notifyTechniciansGlobal(message);
    }

    private void notifyTechniciansGlobal(String plainMessage) {
        for (PrintWriter technicianWriter : technicianGlobalWriters.values()) {
            technicianWriter.println(CryptoUtils.encrypt(plainMessage));
        }
    }
}