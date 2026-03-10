# 📡 Incident Support Chat

Sistema de chat **cliente-servidor** para suporte a incidentes
desenvolvido em **Java**, utilizando **sockets TCP** e **criptografia de
mensagens**.

O sistema simula um ambiente de **Service Desk / NOC**, onde clientes
podem abrir tickets de suporte e técnicos podem assumir o atendimento e
conversar com o cliente em tempo real.

------------------------------------------------------------------------

# 🎯 Objetivo do Projeto

Este projeto foi desenvolvido para aplicar na prática os seguintes
conceitos:

-   Arquitetura **cliente-servidor**
-   Comunicação em rede usando **sockets TCP**
-   **Múltiplos clientes simultâneos**
-   **Criptografia de mensagens**
-   Gerenciamento de **tickets de suporte**
-   Interface gráfica para interação do usuário

------------------------------------------------------------------------

# 🏗 Arquitetura do Sistema

O sistema é dividido em dois componentes principais.

## Servidor

Responsável por:

-   aceitar múltiplas conexões de clientes
-   gerenciar tickets
-   encaminhar mensagens entre cliente e técnico
-   manter controle de técnicos conectados
-   notificar novos tickets

O servidor utiliza:

-   `ServerSocket`
-   `Socket`
-   `Threads`

Cada cliente conectado é tratado em uma **thread independente**,
permitindo múltiplos atendimentos simultâneos.

### Exemplo do servidor aceitando múltiplos clientes

``` java
ServerSocket serverSocket = new ServerSocket(PORT);

while (true) {
    Socket clientSocket = serverSocket.accept();
    new Thread(new ClientHandler(clientSocket, ticketManager)).start();
}
```

------------------------------------------------------------------------

## Cliente

Aplicação com **interface gráfica (Swing)** que permite dois perfis de
uso.

### Cliente

-   abrir ticket de suporte
-   conversar com o técnico responsável

### Técnico

-   visualizar tickets
-   assumir atendimento
-   conversar com cliente
-   participar do chat global de técnicos

------------------------------------------------------------------------

# 🔐 Criptografia

As mensagens trafegadas entre cliente e servidor são criptografadas
antes do envio pela rede.

Neste projeto foi utilizada uma **implementação didática de criptografia
simétrica baseada em XOR**, conforme permitido no enunciado da
atividade.

A criptografia utiliza:

-   operação **XOR**
-   codificação **Base64**

## Funcionamento da criptografia

1.  a mensagem original é convertida em bytes\
2.  cada byte da mensagem é combinado com a chave usando XOR\
3.  o resultado é convertido para Base64\
4.  a mensagem criptografada é enviada pelo socket\
5.  no destino, a mensagem é decodificada e descriptografada com a mesma
    chave

### Fluxo simplificado

    Mensagem original
    ↓
    XOR com a chave
    ↓
    Base64
    ↓
    Envio pela rede
    ↓
    Recepção
    ↓
    Base64 decode
    ↓
    XOR com a mesma chave
    ↓
    Mensagem original

## Chave utilizada

    CHAT_SEGREDO_2026

Implementada na classe:

    CryptoUtils.java

### Trecho da implementação

``` java
private static final String KEY = "CHAT_SEGREDO_2026";

public static String encrypt(String message) {
    byte[] messageBytes = message.getBytes();
    byte[] keyBytes = KEY.getBytes();

    byte[] encrypted = new byte[messageBytes.length];

    for (int i = 0; i < messageBytes.length; i++) {
        encrypted[i] = (byte) (messageBytes[i] ^ keyBytes[i % keyBytes.length]);
    }

    return Base64.getEncoder().encodeToString(encrypted);
}
```

------------------------------------------------------------------------

# 🧠 Funcionalidades

## Sistema de Tickets

Clientes podem abrir tickets informando:

-   nome
-   categoria
-   descrição

Exemplo:

    Categoria: Servidor
    Descrição: servidor caiu

O servidor cria:

    Ticket #1
    Cliente: Sara
    Categoria: Servidor
    Status: ABERTO

------------------------------------------------------------------------

## Atendimento de Tickets

Fluxo:

    Cliente abre ticket
    ↓
    Técnico lista tickets
    ↓
    Técnico assume ticket
    ↓
    Chat cliente ↔ técnico

------------------------------------------------------------------------

## Chat por Ticket

Após assumir o ticket, cliente e técnico podem trocar mensagens.

Exemplo:

    Cliente: O servidor caiu.
    Técnico: Estou verificando agora.
    Cliente: Obrigado.

------------------------------------------------------------------------

## Chat Global entre Técnicos

Canal de colaboração entre técnicos.

Exemplo:

    Gabriel: Alguém pode pegar o ticket 3?
    Loki: Posso assumir.
    Gabriel: Obrigado.

------------------------------------------------------------------------

## Notificação de Novos Tickets

Quando um cliente abre um ticket:

    Sistema: Novo ticket #4 aberto por Sara | Categoria: Servidor

------------------------------------------------------------------------

# 📂 Estrutura do Projeto

    incident-support-chat-java
    │
    ├─ src
    │  └─ br
    │     └─ com
    │        └─ supportchat
    │           ├─ client
    │           │  ├─ ClientGUI.java
    │           │  └─ ClientConnection.java
    │           ├─ server
    │           │  ├─ ServerMain.java
    │           │  ├─ ClientHandler.java
    │           │  └─ TicketManager.java
    │           ├─ model
    │           │  └─ Ticket.java
    │           └─ util
    │              └─ CryptoUtils.java
    │
    └─ out

------------------------------------------------------------------------

# 🚀 Como Executar

## Compilar

    javac -d out src\br\com\supportchat\model\*.java src\br\com\supportchat\util\*.java src\br\com\supportchat\server\*.java src\br\com\supportchat\client\*.java

## Rodar servidor

    java -cp out br.com.supportchat.server.ServerMain

## Rodar cliente

    java -cp out br.com.supportchat.client.ClientGUI

------------------------------------------------------------------------

# 🌐 Comunicação em Rede

    TCP Socket
    Porta: 5000

Para testes em rede local use o IP do servidor em vez de `127.0.0.1`.

Exemplo:

    192.168.0.15

------------------------------------------------------------------------

# 🛠 Tecnologias Utilizadas

-   Java
-   TCP Sockets
-   Java Swing
-   Threads
-   Base64
-   Criptografia XOR

------------------------------------------------------------------------

# 👥 Integrantes do Projeto

- **Sara Thais** – Desenvolvimento do sistema, arquitetura cliente-servidor, criptografia e interface gráfica.
- **Gabriel Penha** – Apresentação do projeto e validação funcional do sistema.