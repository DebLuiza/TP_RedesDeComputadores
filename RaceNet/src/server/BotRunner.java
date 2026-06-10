package server;

import java.util.Random;

public class BotRunner implements Runnable {
    private final RaceState state;
    private final RaceServer raceServer;
    private final Random random = new Random();
    private volatile boolean running = true;
    private int botPosition;

    public BotRunner(RaceState state, RaceServer raceServer) {
        this.state = state;
        this.raceServer = raceServer;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running && !state.hasWinner()) {
            try {
                Thread.sleep(900);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }

            botPosition = Math.min(100, botPosition + 1 + random.nextInt(6));
            state.updatePosition("Bot", botPosition);
            raceServer.broadcastUdpRanking();

            if (botPosition >= 100) {
                raceServer.confirmWinner("Bot");
                return;
            }
        }
    }
}
