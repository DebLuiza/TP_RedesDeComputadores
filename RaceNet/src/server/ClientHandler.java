package server;

import shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RaceState state;
    private final TcpServer tcpServer;
    private final RaceServer raceServer;
    private PrintWriter out;
    private String playerName = "Jogador";

    public ClientHandler(Socket socket, RaceState state, TcpServer tcpServer, RaceServer raceServer) {
        this.socket = socket;
        this.state = state;
        this.tcpServer = tcpServer;
        this.raceServer = raceServer;
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            out = writer;
            String message;
            while ((message = in.readLine()) != null) {
                handle(message);
            }
        } catch (IOException exception) {
            System.err.println("[LOG] Cliente TCP desconectado (" + playerName + "): " + exception.getMessage());
        } finally {
            tcpServer.removeClient(this);
        }
    }

    private void handle(String message) {
        System.out.println("[LOG] TCP recebido de " + playerName + ": " + message);
        String[] parts = Protocol.split(message);
        if (parts.length == 0) {
            return;
        }

        switch (parts[0]) {
            case Protocol.ENTER -> handleEnter(parts);
            case Protocol.READY -> handleReady(parts);
            case Protocol.START_RACE -> raceServer.startRace();
            case Protocol.FINISH -> handleFinish(parts);
            case Protocol.RESET -> raceServer.resetRace();
            default -> send(Protocol.STATUS + ";Comando TCP desconhecido: " + message);
        }
    }

    private void handleEnter(String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            send(Protocol.STATUS + ";Nome inválido.");
            return;
        }
        playerName = parts[1].trim();
        state.addPlayer(playerName);
        send(Protocol.ENTER_OK + ";" + playerName);
        System.out.println("[EVENTO] Jogador " + playerName + " entrou.");
        tcpServer.broadcast(Protocol.STATUS + ";" + playerName + " entrou no jogo.");
    }

    private void handleReady(String[] parts) {
        String name = parts.length >= 2 ? parts[1].trim() : playerName;
        state.markReady(name);
        System.out.println("[EVENTO] Jogador " + name + " está pronto.");
        tcpServer.broadcast(Protocol.STATUS + ";" + name + " está pronto.");
    }

    private void handleFinish(String[] parts) {
        if (parts.length < 2) {
            return;
        }
        raceServer.confirmWinner(parts[1].trim());
    }
}
