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

    private GamePhase phase = GamePhase.TASK_SELECTION;

    private final CardManager cardManager = new CardManager();
    private TaskSelectionManager taskManager;
    private final TrickManager trickManager = new TrickManager();
    private final MissionFactory missionFactory = new MissionFactory(TaskLibrary.getAllTasks());

    // =========================
    // SETUP
    // =========================

    public void createPlayers(int playerCount) {
        players.clear();
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
    }

    // =========================
    // GAME LIFECYCLE
    // =========================

    public void startGame() {
        missions.clear();
        missions.add(missionFactory.createMission(1, 3, players.size()));
        missions.add(missionFactory.createMission(2, 2, players.size()));
        missions.add(missionFactory.createMission(3, 3, players.size()));
        captainIndex = cardManager.determineCaptain(players);
        startTaskSelectionPhase();
    }

    public void dealCards() {
        cardManager.dealCards(players);
    }

    private void startTaskSelectionPhase() {
        phase = GamePhase.TASK_SELECTION;
        currentPlayerIndex = captainIndex;
        this.taskManager = new TaskSelectionManager(
                players,
                getCurrentMission().getTasks()
        );
    }

    // =========================
    // TASK SELECTION
    // =========================

    public boolean selectTask(int playerIndex, Task task) {
        if (phase != GamePhase.TASK_SELECTION || taskManager == null) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            return false;
        }

        taskManager.selectTask(playerIndex, task);

        if (taskManager.isSelectionFinished()) {
            startTrickPhase();
        } else {
            nextPlayer();
        }

        return true;
    }

    public boolean passTaskSelection(int playerIndex) {
        if (phase != GamePhase.TASK_SELECTION || taskManager == null) {
            return false;
        }

        if (!taskManager.canSkip(playerIndex, currentPlayerIndex)) {
            return false;
        }

        taskManager.pass();
        nextPlayer();

        return true;
    }

    // =========================
    // TRICK PHASE
    // =========================

    private void startTrickPhase() {

        phase = GamePhase.TRICKING;
        currentPlayerIndex = captainIndex;
        trickManager.reset();
    }

    public boolean playCard(int playerIndex, Card card) {

        if (phase != GamePhase.TRICKING) {
            return false;
        }

        if (playerIndex != currentPlayerIndex) {
            return false;
        }

        Player player = players.get(playerIndex);

        if (!player.getHand().contains(card)) {
            return false;
        }

        // ask trick manager to validate + record
        if (!trickManager.playCard(playerIndex, card, player.getHand())) {
            return false;
        }

        // remove ONLY after validation success
        player.removeCardFromHand(card);

        // trick finished?
        if (trickManager.isComplete(players.size())) {

            currentPlayerIndex = trickManager.getWinner();

            trickManager.reset();

        } else {
            nextPlayer();
        }

        return true;
    }

    // =========================
    // STATE ACCESSORS & HELPERS
    // =========================

    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getCaptainIndex() {
        return captainIndex;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Mission getCurrentMission() {
        if (missions.isEmpty()) {
            return null;
        }
        return missions.get(currentMissionIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }
}