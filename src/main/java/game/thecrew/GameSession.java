package game.thecrew;

import game.thecrew.engine.CrewEngine;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;

import java.util.List;

public class GameSession {

    private final int playerCount;
    private int currentMissionNumber;
    private final CrewEngine engine;

    public GameSession(int playerCount) {
        if (playerCount < 3 || playerCount > 5) {
            throw new IllegalArgumentException("Player count must be between 3 and 5, got: " + playerCount);
        }
        this.playerCount = playerCount;
        this.currentMissionNumber = 1;
        this.engine = new CrewEngine();
    }

    public void start() {
        engine.createPlayers(playerCount);
        engine.dealCards();
        engine.startGame();
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public List<Player> getPlayers() {
        return engine.getPlayers();
    }

    public int getCurrentMissionNumber() {
        return currentMissionNumber;
    }

    public Mission getCurrentMission() {
        return engine.getCurrentMission();
    }

    public CrewEngine getEngine() {
        return engine;
    }
}