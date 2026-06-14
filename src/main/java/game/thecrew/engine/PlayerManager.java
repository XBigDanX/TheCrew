package game.thecrew.engine;

import game.thecrew.model.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int captainIndex = 0;
    private final CardManager cardManager;

    public PlayerManager(CardManager cardManager) {
        this.cardManager = cardManager;
    }

    public void createPlayers(int playerCount) {
        players.clear();
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void dealCards() {
        cardManager.dealCards(players);
    }

    public void clearHands() {
        for (Player player : players) {
            player.getHand().clear();
            player.getTaskHand().clear();
        }
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int index) {
        currentPlayerIndex = index;
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public int getCaptainIndex() {
        return captainIndex;
    }

    public void setCaptainIndex(int index) {
        captainIndex = index;
    }
}
