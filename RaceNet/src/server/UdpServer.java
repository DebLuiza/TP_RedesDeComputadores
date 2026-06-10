package server;

import shared.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpServer implements Runnable {
    private final int port;
    private final RaceState state;
    private final RaceServer raceServer;
    private final Map<String, InetSocketAddress> udpClients = new ConcurrentHashMap<>();
    private DatagramSocket socket;

    public UdpServer(int port, RaceState state, RaceServer raceServer) {
        this.port = port;
        this.state = state;
        this.raceServer = raceServer;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        try (DatagramSocket datagramSocket = new DatagramSocket(port)) {
            socket = datagramSocket;
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                handle(message, packet);
            }
        } catch (IOException exception) {
            System.err.println("Erro no servidor UDP: " + exception.getMessage());
        }
    }

    private void handle(String message, DatagramPacket packet) {
        System.out.println("[UDP recebido] " + message);
        String[] parts = Protocol.split(message);
        if (parts.length < 3 || !Protocol.POSITION.equals(parts[0])) {
            return;
        }

        String name = parts[1].trim();
        udpClients.put(name, new InetSocketAddress(packet.getAddress(), packet.getPort()));

        try {
            int position = Integer.parseInt(parts[2].trim());
            int officialPosition = state.updatePosition(name, position);
            broadcastRanking();
            if (officialPosition >= 100) {
                raceServer.confirmWinner(name);
            }
        } catch (NumberFormatException exception) {
            send(Protocol.STATUS + ";Posição inválida.", packet.getSocketAddress());
        }
    }

    public void broadcastRanking() {
        String message = state.rankingMessage();
        for (InetSocketAddress address : udpClients.values()) {
            send(message, address);
        }
        System.out.println("[UDP] " + message);
    }

    private void send(String message, java.net.SocketAddress address) {
        if (socket == null || address == null) {
            return;
        }

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data, data.length, address);
        try {
            socket.send(packet);
        } catch (IOException exception) {
            System.err.println("Erro ao enviar UDP: " + exception.getMessage());
        }
    }
}
