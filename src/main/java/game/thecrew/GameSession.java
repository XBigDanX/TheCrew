package game.thecrew;

import game.thecrew.engine.CrewEngine;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;

import java.util.List;

public class GameSession {

    private final int playerCount;
    private final CrewEngine engine;

    public GameSession(int playerCount) {
        if (playerCount < 2 || playerCount > 5) {
            throw new IllegalArgumentException("Player count must be between 2 and 5, got: " + playerCount);
        }
        this.playerCount = playerCount;
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
        return engine.getPlayerManager().getPlayers();
    }

    public Mission getCurrentMission() {
        return engine.getCurrentMission();
    }

    public CrewEngine getEngine() {
        return engine;
    }
}