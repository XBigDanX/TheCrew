package game.thecrew.engine;

import game.thecrew.mission.MissionFactory;
import game.thecrew.mission.TaskLibrary;
import game.thecrew.model.*;

import java.util.ArrayList;
import java.util.List;

public class CrewEngine {

    private final List<Player> players = new ArrayList<>();

    private final List<Mission> missions = new ArrayList<>();
    private int currentMissionIndex = 0;

    private int currentPlayerIndex = 0;
    private int captainIndex = 0;

    private GamePhase phase = GamePhase.TASK_SELECTION;

    private int cardsPlayedInMission = 0;

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
        missions.add(missionFactory.createMission(1, 1, players.size()));
        missions.add(missionFactory.createMission(2, 2, players.size()));
        missions.add(missionFactory.createMission(3, 3, players.size()));
        captainIndex = cardManager.determineCaptain(players);
        cardsPlayedInMission = 0;
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

    public boolean selectTask(int playerIndex, ActiveMissionTask task) {
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
        cardsPlayedInMission++;

        // trick finished?
        if (trickManager.isComplete(players.size())) {

            Trick completed = trickManager.getCurrentTrick();
            getCurrentMission().addCompletedTrick(completed);

            currentPlayerIndex = trickManager.getWinner();

            trickManager.reset();

            // After trick is fully processed, check if the mission is over
            if (cardsPlayedInMission >= getExpectedCardsToBePlayed()) {
                evaluateMissionEnd();
                phase = GamePhase.MISSION_COMPLETE;
            }

        } else {
            nextPlayer();
        }

        return true;
    }

    // =========================
    // MISSION EVALUATION
    // =========================

    private int getExpectedCardsToBePlayed() {
        int count = players.size();
        if (count == 3) return 39;
        return 40;
    }

    // =========================
    // MISSION FLOW CONTROL
    // =========================

    public void advanceToNextMission() {
        currentMissionIndex++;
        if (currentMissionIndex >= missions.size()) {
            phase = GamePhase.MISSION_COMPLETE;
            return;
        }
        cardsPlayedInMission = 0;
        clearAndRedeal();
        captainIndex = cardManager.determineCaptain(players);
        getCurrentMission().setStatus(MissionStatus.IN_PROGRESS);
        startTaskSelectionPhase();
    }

    public void restartCurrentMission() {
        Mission current = getCurrentMission();
        Mission fresh = missionFactory.createMission(current.getId(), current.getDifficulty(), players.size());
        missions.set(currentMissionIndex, fresh);
        cardsPlayedInMission = 0;
        clearAndRedeal();
        captainIndex = cardManager.determineCaptain(players);
        startTaskSelectionPhase();
    }

    public boolean isGameOver() {
        return currentMissionIndex >= missions.size() - 1;
    }

    private void clearAndRedeal() {
        for (Player player : players) {
            player.getHand().clear();
            player.getTaskHand().clear();
        }
        dealCards();
    }

    private void evaluateMissionEnd() {
        Mission mission = getCurrentMission();
        // Evaluate non-trick-based tasks that trigger at mission end
        for (ActiveMissionTask task : mission.getTasks()) {
            task.checkMissionEnd(mission);
        }
        // All tasks must be completed for success
        boolean allCompleted = true;
        for (ActiveMissionTask task : mission.getTasks()) {
            if (!task.isCompleted()) {
                allCompleted = false;
                break;
            }
        }
        mission.setStatus(allCompleted ? MissionStatus.SUCCESS : MissionStatus.FAILED);
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

    public int getCurrentMissionNumber() {
        return currentMissionIndex + 1;
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

    public TrickManager getTrickManager() {
        return trickManager;
    }
}