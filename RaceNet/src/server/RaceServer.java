package server;

import shared.Protocol;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaceServer {
    private final RaceState state = new RaceState();
    private final TcpServer tcpServer;
    private final UdpServer udpServer;
    private BotRunner botRunner;

    public RaceServer(int tcpPort, int udpPort) {
        tcpServer = new TcpServer(tcpPort, state, this);
        udpServer = new UdpServer(udpPort, state, this);
    }

    public void start() {
        new Thread(udpServer, "UDP-Server").start();
        new Thread(tcpServer, "TCP-Server").start();
        System.out.println("[EVENTO] RaceNet Server iniciado.");
        System.out.println("[LOG] TCP: " + tcpServer.getPort());
        System.out.println("[LOG] UDP: " + udpServer.getPort());
        System.out.println("[LOG] IPs locais para clientes: " + localIPv4Addresses());
        System.out.println("[LOG] Informe no cliente o IP do servidor e as portas TCP/UDP acima.");
    }

    public synchronized void startRace() {
        if (state.isRaceStarted()) {
            return;
        }

        if (!state.allPlayersReady()) {
            System.out.println("[LOG] Tentativa de início falhou: nem todos estão prontos.");
            tcpServer.broadcast(Protocol.STATUS + ";A corrida só inicia quando TODOS estiverem prontos.");
            return;
        }

        System.out.println("[EVENTO] Iniciando corrida com " + state.playerCount() + " jogadores.");
        if (state.playerCount() == 1 && !state.hasPlayer("Bot")) {
            state.addPlayer("Bot");
            botRunner = new BotRunner(state, this);
            new Thread(botRunner, "Bot-Runner").start();
            tcpServer.broadcast(Protocol.STATUS + ";Bot criado para competir.");
        }

        state.startRace();
        tcpServer.broadcast(Protocol.START_RACE);
        udpServer.broadcastRanking();
    }

    public synchronized void resetRace() {
        System.out.println("[EVENTO] Resetando corrida...");
        state.reset();
        if (botRunner != null) {
            botRunner.stop();
            botRunner = null;
        }
        udpServer.clearClients();
        tcpServer.broadcast(Protocol.RESET);
        tcpServer.broadcast(Protocol.STATUS + ";Corrida resetada. Todos devem se conectar novamente.");
    }

    public synchronized void confirmWinner(String name) {
        if (state.setWinner(name)) {
            System.out.println("[EVENTO] Vencedor confirmado: " + name);
            if (botRunner != null) {
                botRunner.stop();
            }
            tcpServer.broadcast(Protocol.WINNER + ";" + name);
            udpServer.broadcastRanking();
        }
    }

    public void broadcastUdpRanking() {
        udpServer.broadcastRanking();
    }

    public static void main(String[] args) {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : Protocol.DEFAULT_TCP_PORT;
        int udpPort = args.length > 1 ? Integer.parseInt(args[1]) : Protocol.DEFAULT_UDP_PORT;
        new RaceServer(tcpPort, udpPort).start();
    }

    private List<String> localIPv4Addresses() {
        List<String> addresses = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (var address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address) {
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException exception) {
            addresses.add("erro ao listar IPs: " + exception.getMessage());
        }
        return addresses.isEmpty() ? List.of("127.0.0.1") : addresses;
    }
}
