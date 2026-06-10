package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TcpServer implements Runnable {
    private final int port;
    private final RaceState state;
    private final RaceServer raceServer;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public TcpServer(int port, RaceState state, RaceServer raceServer) {
        this.port = port;
        this.state = state;
        this.raceServer = raceServer;
    }

    public int getPort() {
        return port;
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcast(String message) {
        System.out.println("[TCP] " + message);
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, state, this, raceServer);
                addClient(handler);
                new Thread(handler, "Client-" + socket.getRemoteSocketAddress()).start();
            }
        } catch (IOException exception) {
            System.err.println("Erro no servidor TCP: " + exception.getMessage());
        }
    }
}
