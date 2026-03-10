package br.com.supportchat.server;

import br.com.supportchat.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class TicketManager {

    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 1;

    public synchronized Ticket createTicket(String clienteNome, String categoria, String descricao) {
        Ticket ticket = new Ticket(nextId++, clienteNome, categoria, descricao);
        tickets.add(ticket);
        return ticket;
    }

    public synchronized List<Ticket> listTickets() {
        return new ArrayList<>(tickets);
    }

    public synchronized Ticket findById(int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        return null;
    }

    public synchronized boolean assignTechnician(int ticketId, String tecnicoNome) {
        Ticket ticket = findById(ticketId);

        if (ticket == null) {
            return false;
        }

        if (!"ABERTO".equals(ticket.getStatus()) && !"EM_ATENDIMENTO".equals(ticket.getStatus())) {
            return false;
        }

        if (ticket.getTecnicoNome() == null) {
            ticket.setTecnicoNome(tecnicoNome);
            ticket.setStatus("EM_ATENDIMENTO");
            return true;
        }

        return ticket.getTecnicoNome().equals(tecnicoNome);
    }

    public synchronized boolean resolveTicket(int ticketId, String tecnicoNome) {
        Ticket ticket = findById(ticketId);

        if (ticket == null) {
            return false;
        }

        if (!"EM_ATENDIMENTO".equals(ticket.getStatus())) {
            return false;
        }

        if (!tecnicoNome.equals(ticket.getTecnicoNome())) {
            return false;
        }

        ticket.setStatus("RESOLVIDO");
        return true;
    }
}