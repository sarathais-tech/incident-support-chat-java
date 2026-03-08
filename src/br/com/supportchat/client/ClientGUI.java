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

    private final JTextArea txtDescricao = new JTextArea(4, 30);
    private final JTextArea txtSaida = new JTextArea(10, 40);
    private final JTextArea txtChat = new JTextArea(10, 40);

    private final JTextField txtTicketId = new JTextField(10);
    private final JTextField txtMensagem = new JTextField(30);

    private final JButton btnAbrirTicket = new JButton("Abrir Ticket");
    private final JButton btnListarTickets = new JButton("Listar Tickets");
    private final JButton btnAssumirTicket = new JButton("Assumir Ticket");
    private final JButton btnConectarChat = new JButton("Conectar Chat");
    private final JButton btnEnviarMensagem = new JButton("Enviar Mensagem");

    private ClientConnection persistentConnection;
    private Integer currentChatTicketId = null;

    public ClientGUI() {
        setTitle("Incident Support Chat");
        setSize(800, 720);
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

        JPanel centerPanel = new JPanel(new GridLayout(3, 1));

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

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat do Ticket"));
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);

        JPanel chatInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chatInputPanel.add(new JLabel("Mensagem:"));
        txtMensagem.setPreferredSize(new Dimension(400, 25));
        chatInputPanel.add(txtMensagem);
        chatInputPanel.add(btnConectarChat);
        chatInputPanel.add(btnEnviarMensagem);

        chatPanel.add(new JScrollPane(txtChat), BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        centerPanel.add(descriptionPanel);
        centerPanel.add(outputPanel);
        centerPanel.add(chatPanel);

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
        btnConectarChat.addActionListener(e -> conectarChat());
        btnEnviarMensagem.addActionListener(e -> enviarMensagemChat());
    }

    private void updateMode() {
        boolean cliente = rbCliente.isSelected();

        cbCategoria.setEnabled(cliente);
        txtDescricao.setEnabled(cliente);
        btnAbrirTicket.setEnabled(cliente);

        btnListarTickets.setEnabled(!cliente);
        btnAssumirTicket.setEnabled(!cliente);
        txtTicketId.setEnabled(true);
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

        if (response.startsWith("OK|Ticket criado com ID ")) {
            String id = response.replace("OK|Ticket criado com ID ", "").trim();
            txtTicketId.setText(id);
        }
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

    private void conectarChat() {
    String nome = txtNome.getText().trim();
    String ticketIdText = txtTicketId.getText().trim();

    if (nome.isEmpty()) {
        appendOutput("Informe o nome.");
        return;
    }

    if (ticketIdText.isEmpty()) {
        appendOutput("Informe o ID do ticket.");
        return;
    }

    int ticketId;
    try {
        ticketId = Integer.parseInt(ticketIdText);
    } catch (NumberFormatException e) {
        appendOutput("ID do ticket inválido.");
        return;
    }

    if (persistentConnection != null) {
        persistentConnection.disconnect();
    }

    persistentConnection = createConnection();
    if (persistentConnection == null) {
        return;
    }

    currentChatTicketId = ticketId;
    txtChat.setText("");
    appendChat("Chat conectado ao ticket " + ticketId + ".");

    String response;

    if (rbTecnico.isSelected()) {
        response = persistentConnection.sendAndReceive("ASSUMIR|" + nome + "|" + ticketId);
        appendOutput("Resposta do servidor: " + response);
    } else {
        response = persistentConnection.sendAndReceive("REGISTRAR_CLIENTE|" + nome + "|" + ticketId);
        appendOutput("Resposta do servidor: " + response);
    }

    persistentConnection.startListening(new ClientConnection.MessageListener() {
        @Override
        public void onMessageReceived(String message) {
            SwingUtilities.invokeLater(() -> handleIncomingMessage(message));
        }

        @Override
        public void onError(String error) {
            SwingUtilities.invokeLater(() -> appendOutput(error));
        }
    });
}
    private void enviarMensagemChat() {
        if (persistentConnection == null || currentChatTicketId == null) {
            appendOutput("Conecte o chat primeiro.");
            return;
        }

        String nome = txtNome.getText().trim();
        String mensagem = txtMensagem.getText().trim();

        if (nome.isEmpty()) {
            appendOutput("Informe o nome.");
            return;
        }

        if (mensagem.isEmpty()) {
            return;
        }

        String request = "CHAT|" + nome + "|" + currentChatTicketId + "|" + mensagem;
        persistentConnection.sendOnly(request);

        appendChat("Você: " + mensagem);
        txtMensagem.setText("");
    }

    private void handleIncomingMessage(String message) {
        if (message.startsWith("CHAT|")) {
            String[] parts = message.split("\\|", 3);
            if (parts.length == 3) {
                appendChat(parts[1] + ": " + parts[2]);
            }
        } else {
            appendOutput("Resposta do servidor: " + message);
        }
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

    private void appendChat(String text) {
        txtChat.append(text + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}