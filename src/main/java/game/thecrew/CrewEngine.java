package game.thecrew;

import game.thecrew.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CrewEngine {

    private final List<Player> players = new ArrayList<>();

    private final List<Mission> missions = new ArrayList<>();
    private int currentMissionIndex = 0;

    private int currentPlayerIndex = 0;

    private GamePhase phase = GamePhase.TASK_SELECTION;

    // =========================
    // MISSIONS
    // =========================

    public void addMission(Mission mission) {
        missions.add(mission);
    }

    public Mission getCurrentMission() {

        if (missions.isEmpty()) {
            return null;
        }

        return missions.get(currentMissionIndex);
    }

    public void nextMission() {

        if (currentMissionIndex < missions.size() - 1) {
            currentMissionIndex++;
        }

        currentPlayerIndex = 0;
        phase = GamePhase.TASK_SELECTION;
    }

    public int getCurrentMissionIndex() {
        return currentMissionIndex;
    }

    // =========================
    // PLAYERS
    // =========================

    public void createPlayers(int playerCount) {

        players.clear();

        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    // =========================
    // TURN SYSTEM
    // =========================

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // =========================
    // PHASE
    // =========================

    public GamePhase getPhase() {
        return phase;
    }

    // =========================
    // TASK LOGIC
    // =========================

    public boolean selectTask(int playerIndex, Task task) {

        if (phase != GamePhase.TASK_SELECTION) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            return false;
        }

        Mission mission = getCurrentMission();

        if (mission == null) {
            return false;
        }

        Player player = players.get(playerIndex);

        mission.getTasks().remove(task);
        player.addTask(task);

        nextTurn();

        if (mission.getTasks().isEmpty()) {
            phase = GamePhase.TRICKING;
            currentPlayerIndex = 0;
        }

        return true;
    }

    private int calculateRemainingTasks() {
        Mission mission = getCurrentMission();
        if (mission == null) return 0;
        return mission.getTasks().size();
    }

    // =========================
    // CARD LOGIC
    // =========================

    public boolean playCard(int playerIndex, Card card) {

        if (phase != GamePhase.TRICKING) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            return false;
        }

        Player player = players.get(playerIndex);

        player.removeCardFromHand(card);

        nextTurn();

        return true;
    }

    private boolean canPassTaskSelection() {

        if (phase != GamePhase.TASK_SELECTION) return false;

        int remainingTasks = calculateRemainingTasks();
        int remainingPlayers = players.size() - currentPlayerIndex;

        return remainingTasks < remainingPlayers;
    }

    public boolean passTaskSelection(int playerIndex) {

        if (phase != GamePhase.TASK_SELECTION) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            return false;
        }

        if (!canPassTaskSelection()) return false;

        nextTurn();

        return true;
    }

    private List<Card> createDeck() {

        List<Card> deck = new ArrayList<>();

        // Standard Crew-style deck:
        // Colors + values 1–9 + submarine trump cards

        CardColor[] colors = {
                CardColor.BLUE,
                CardColor.RED,
                CardColor.YELLOW,
                CardColor.GREEN
        };

        for (CardColor color : colors) {
            for (int value = 1; value <= 9; value++) {
                deck.add(new Card(color, value));
            }
        }

        // Add submarines (trump cards)
        for (int i = 1; i <= 4; i++) {
            deck.add(new Card(CardColor.SUBMARINE, i));
        }

        return deck;
    }

    public void dealCards() {

        List<Card> deck = createDeck();
        Collections.shuffle(deck, new Random());

        int playerIndex = 0;

        while (!deck.isEmpty()) {

            Card card = deck.remove(deck.size() - 1);

            players.get(playerIndex).addCardToHand(card);

            playerIndex = (playerIndex + 1) % players.size();
        }
    }
}