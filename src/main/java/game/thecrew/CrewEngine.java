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
    private int captainIndex = 0;

    private int turnsRemainingInCycle = 0;

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

        startTaskSelectionPhase();
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
    // PHASE
    // =========================

    public GamePhase getPhase() {
        return phase;
    }

    // =========================
    // TURN SYSTEM
    // =========================

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // =========================
    // TASK SELECTION
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

        turnsRemainingInCycle--;

        if (mission.getTasks().isEmpty()) {
            startTrickPhase();
            return true;
        }

        advanceTaskSelectionTurn();

        return true;
    }

    public boolean passTaskSelection(int playerIndex) {

        if (phase != GamePhase.TASK_SELECTION) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            return false;
        }

        if (!canPassTaskSelection()) {
            return false;
        }

        turnsRemainingInCycle--;

        advanceTaskSelectionTurn();

        return true;
    }

    private boolean canPassTaskSelection() {

        Mission mission = getCurrentMission();

        if (mission == null) {
            return false;
        }

        int remainingTasks = mission.getTasks().size();

        int remainingPlayersAfterPass = turnsRemainingInCycle - 1;

        return remainingTasks <= remainingPlayersAfterPass;
    }


    private void advanceTaskSelectionTurn() {

        Mission mission = getCurrentMission();

        if (mission == null) {
            return;
        }

        if (mission.getTasks().isEmpty()) {
            startTrickPhase();
            return;
        }

        if (turnsRemainingInCycle == 0) {

            turnsRemainingInCycle = players.size();
            currentPlayerIndex = captainIndex;

            return;
        }

        nextPlayer();
    }

    // =========================
    // TRICK PHASE
    // =========================

    private void startTrickPhase() {

        phase = GamePhase.TRICKING;
        currentPlayerIndex = captainIndex;
    }

    // =========================
    // CARD PLAY
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

        nextPlayer();

        return true;
    }

    // =========================
    // DECK
    // =========================

    private List<Card> createDeck() {

        List<Card> deck = new ArrayList<>();

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

    // =========================
    // GAME START
    // =========================

    public void startGame() {

        determineCaptain();
        startTaskSelectionPhase();
    }

    private void startTaskSelectionPhase() {

        phase = GamePhase.TASK_SELECTION;

        currentPlayerIndex = captainIndex;

        turnsRemainingInCycle = players.size();
    }

    // =========================
    // CAPTAIN
    // =========================

    private void determineCaptain() {

        for (int i = 0; i < players.size(); i++) {

            for (Card card : players.get(i).getHand()) {

                if (card.getColor() == CardColor.SUBMARINE
                        && card.getValue() == 4) {

                    captainIndex = i;
                    return;
                }
            }
        }

        captainIndex = 0;
    }

    public int getCaptainIndex() {
        return captainIndex;
    }
}