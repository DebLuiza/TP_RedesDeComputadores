package server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RaceState {
    private final Map<String, Integer> positions = new LinkedHashMap<>();
    private final Set<String> readyPlayers = new LinkedHashSet<>();
    private boolean raceStarted;
    private String winner;

    public synchronized void addPlayer(String name) {
        positions.putIfAbsent(name, 0);
    }

    public synchronized void markReady(String name) {
        addPlayer(name);
        readyPlayers.add(name);
    }

    public synchronized boolean hasPlayer(String name) {
        return positions.containsKey(name);
    }

    public synchronized int playerCount() {
        return positions.size();
    }

    public synchronized List<String> playerNames() {
        return new ArrayList<>(positions.keySet());
    }

    public synchronized void startRace() {
        raceStarted = true;
    }

    public synchronized boolean isRaceStarted() {
        return raceStarted;
    }

    public synchronized boolean hasWinner() {
        return winner != null;
    }

    public synchronized String getWinner() {
        return winner;
    }

    public synchronized boolean setWinner(String name) {
        if (winner != null) {
            return false;
        }
        winner = name;
        positions.put(name, 100);
        return true;
    }

    public synchronized int updatePosition(String name, int position) {
        addPlayer(name);
        int officialPosition = Math.max(0, Math.min(100, position));
        positions.put(name, officialPosition);
        return officialPosition;
    }

    public synchronized String rankingMessage() {
        StringBuilder builder = new StringBuilder("RANKING");
        for (Map.Entry<String, Integer> entry : positions.entrySet()) {
            builder.append(';')
                    .append(entry.getKey())
                    .append('=')
                    .append(entry.getValue());
        }
        return builder.toString();
    }
}
