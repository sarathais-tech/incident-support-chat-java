package br.com.supportchat.client;

import java.awt.*;
import javax.swing.*;

public class ClientGUI extends JFrame {

    private final JTextField txtNome = new JTextField(15);
    private final JTextField txtHost = new JTextField("127.0.0.1", 12);
    private final JTextField txtPorta = new JTextField("5000", 6);

    private final JRadioButton rbCliente = new JRadioButton("Cliente", true);
    private final JRadioButton rbTecnico = new JRadioButton("Técnico");

    private final JComboBox<String> cbCategoria = new JComboBox<>(new String[]{
            "Servidor", "Banco de Dados", "Rede", "Aplicação", "Segurança"
    });

    private final JTextArea txtDescricao = new JTextArea(5, 30);
    private final JTextArea txtSaida = new JTextArea(15, 40);
    private final JTextField txtTicketId = new JTextField(10);

    private final JButton btnAbrirTicket = new JButton("Abrir Ticket");
    private final JButton btnListarTickets = new JButton("Listar Tickets");
    private final JButton btnAssumirTicket = new JButton("Assumir Ticket");

    public ClientGUI() {
        setTitle("Incident Support Chat");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildInterface();
        configureEvents();
        updateMode();
    }

    private void buildInterface() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 1));

        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Conexão"));
        connectionPanel.add(new JLabel("Nome:"));
        connectionPanel.add(txtNome);
        connectionPanel.add(new JLabel("Host:"));
        connectionPanel.add(txtHost);
        connectionPanel.add(new JLabel("Porta:"));
        connectionPanel.add(txtPorta);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setBorder(BorderFactory.createTitledBorder("Perfil"));
        ButtonGroup group = new ButtonGroup();
        group.add(rbCliente);
        group.add(rbTecnico);
        rolePanel.add(rbCliente);
        rolePanel.add(rbTecnico);

        JPanel ticketPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Ticket"));
        ticketPanel.add(new JLabel("Categoria:"));
        ticketPanel.add(cbCategoria);
        ticketPanel.add(new JLabel("Ticket ID:"));
        txtTicketId.setPreferredSize(new Dimension(80, 25));
        ticketPanel.add(txtTicketId);

        topPanel.add(connectionPanel);
        topPanel.add(rolePanel);
        topPanel.add(ticketPanel);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder("Descrição"));
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        descriptionPanel.add(new JScrollPane(txtDescricao), BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Saída"));
        txtSaida.setEditable(false);
        txtSaida.setLineWrap(true);
        txtSaida.setWrapStyleWord(true);
        outputPanel.add(new JScrollPane(txtSaida), BorderLayout.CENTER);

        centerPanel.add(descriptionPanel, BorderLayout.NORTH);
        centerPanel.add(outputPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnAbrirTicket);
        buttonPanel.add(btnListarTickets);
        buttonPanel.add(btnAssumirTicket);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void configureEvents() {
        rbCliente.addActionListener(e -> updateMode());
        rbTecnico.addActionListener(e -> updateMode());

        btnAbrirTicket.addActionListener(e -> abrirTicket());
        btnListarTickets.addActionListener(e -> listarTickets());
        btnAssumirTicket.addActionListener(e -> assumirTicket());
    }

    private void updateMode() {
        boolean cliente = rbCliente.isSelected();

        cbCategoria.setEnabled(cliente);
        txtDescricao.setEnabled(cliente);
        btnAbrirTicket.setEnabled(cliente);

        btnListarTickets.setEnabled(!cliente);
        btnAssumirTicket.setEnabled(!cliente);
        txtTicketId.setEnabled(!cliente);
    }

    private ClientConnection createConnection() {
        String host = txtHost.getText().trim();
        String portText = txtPorta.getText().trim();

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            appendOutput("Porta inválida.");
            return null;
        }

        ClientConnection connection = new ClientConnection();
        boolean connected = connection.connect(host, port);

        if (!connected) {
            appendOutput("Não foi possível conectar ao servidor.");
            return null;
        }

        return connection;
    }

    private void abrirTicket() {
        String nome = txtNome.getText().trim();
        String categoria = (String) cbCategoria.getSelectedItem();
        String descricao = txtDescricao.getText().trim();

        if (nome.isEmpty()) {
            appendOutput("Informe o nome.");
            return;
        }

        if (descricao.isEmpty()) {
            appendOutput("Informe a descrição do problema.");
            return;
        }

        ClientConnection connection = createConnection();
        if (connection == null) {
            return;
        }

        String request = "TICKET|" + nome + "|" + categoria + "|" + descricao;
        String response = connection.sendAndReceive(request);
        connection.disconnect();

        appendOutput("Resposta do servidor: " + response);
    }

    private void listarTickets() {
        String nome = txtNome.getText().trim();

        if (nome.isEmpty()) {
            appendOutput("Informe o nome do técnico.");
            return;
        }

        ClientConnection connection = createConnection();
        if (connection == null) {
            return;
        }

        String response = connection.sendAndReceive("LISTAR|");
        connection.disconnect();

        formatListResponse(response);
    }

    private void assumirTicket() {
        String nome = txtNome.getText().trim();
        String ticketId = txtTicketId.getText().trim();

        if (nome.isEmpty()) {
            appendOutput("Informe o nome do técnico.");
            return;
        }

        if (ticketId.isEmpty()) {
            appendOutput("Informe o ID do ticket.");
            return;
        }

        ClientConnection connection = createConnection();
        if (connection == null) {
            return;
        }

        String request = "ASSUMIR|" + nome + "|" + ticketId;
        String response = connection.sendAndReceive(request);
        connection.disconnect();

        appendOutput("Resposta do servidor: " + response);
    }

    private void formatListResponse(String response) {
        txtSaida.setText("");

        if (response.startsWith("LISTA|")) {
            String content = response.substring(6);

            if (content.equals("Nenhum ticket cadastrado.")) {
                appendOutput(content);
                return;
            }

            String[] tickets = content.split("#");

            appendOutput("Tickets encontrados:");
            appendOutput("------------------------------");

            for (String ticket : tickets) {
                if (ticket.isBlank()) {
                    continue;
                }

                String[] fields = ticket.split(";");
                if (fields.length >= 5) {
                    appendOutput("ID: " + fields[0]);
                    appendOutput("Cliente: " + fields[1]);
                    appendOutput("Categoria: " + fields[2]);
                    appendOutput("Status: " + fields[3]);
                    appendOutput("Técnico: " + (fields[4].equals("-") ? "Nenhum" : fields[4]));
                    appendOutput("------------------------------");
                }
            }
        } else {
            appendOutput("Resposta do servidor: " + response);
        }
    }

    private void appendOutput(String text) {
        txtSaida.append(text + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}