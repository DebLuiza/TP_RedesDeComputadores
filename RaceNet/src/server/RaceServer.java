package server;

import shared.Protocol;

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
        System.out.println("RaceNet Server iniciado.");
        System.out.println("TCP: " + tcpServer.getPort());
        System.out.println("UDP: " + udpServer.getPort());
    }

    public synchronized void startRace() {
        if (state.isRaceStarted()) {
            return;
        }

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

    public synchronized void confirmWinner(String name) {
        if (state.setWinner(name)) {
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
}
