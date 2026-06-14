package game.thecrew.engine;

import game.thecrew.mission.MissionLibrary;
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
    private CommunicationToken[] pendingTokens;
    private int communicationPlayerIndex = -1;
    private boolean[] communicationRequested;

    // =========================
    // SETUP
    // =========================

    public void createPlayers(int playerCount) {
        players.clear();
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
        pendingTokens = new CommunicationToken[playerCount];
        communicationRequested = new boolean[playerCount];
    }

    // =========================
    // GAME LIFECYCLE
    // =========================

    public void startGame() {
        missions.clear();
        int pc = players.size();
        captainIndex = cardManager.determineCaptain(players);
        for (int id = 1; id <= 32; id++) {
            Mission m = MissionLibrary.forPlayerCount(id, pc);
            m.setCaptainIndex(captainIndex);
            missions.add(m);
        }
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
        // Check if any player queued a communication request during task selection
        for (int i = 0; i < communicationRequested.length; i++) {
            if (communicationRequested[i]) {
                this.phase = GamePhase.COMMUNICATION;
                this.communicationPlayerIndex = i;
                return;
            }
        }
        phase = GamePhase.TRICKING;
        currentPlayerIndex = captainIndex;
        applyPendingTokens();
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

            // CASE A — Early win: all tasks completed before all cards played
            if (allTasksCompleted()) {
                evaluateMissionEnd();
                phase = GamePhase.MISSION_COMPLETE;
                trickManager.reset();
                return true;
            }

            currentPlayerIndex = trickManager.getWinner();

            applyPendingTokens();
            trickManager.reset();

            // CASE B — Full completion fallback when all cards are exhausted
            if (cardsPlayedInMission >= getExpectedCardsToBePlayed()) {
                evaluateMissionEnd();
                phase = GamePhase.MISSION_COMPLETE;
            }

            // After trick completes, check for queued communication requests
            if (phase == GamePhase.TRICKING) {
                for (int i = 0; i < communicationRequested.length; i++) {
                    if (communicationRequested[i]) {
                        phase = GamePhase.COMMUNICATION;
                        communicationPlayerIndex = i;
                        break;
                    }
                }
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
        if (count == 2) return 40;
        if (count == 3) return 39;
        return 40;
    }

    private boolean allTasksCompleted() {
        Mission mission = getCurrentMission();
        if (mission == null) return false;
        for (Task task : mission.getTasks()) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        return true;
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
        Mission fresh = MissionLibrary.forPlayerCount(current.getId(), players.size());
        fresh.setCaptainIndex(captainIndex);
        missions.set(currentMissionIndex, fresh);
        cardsPlayedInMission = 0;
        pendingTokens = new CommunicationToken[players.size()];
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
        for (Task task : mission.getTasks()) {
            task.checkMissionEnd(mission);
        }
        boolean allCompleted = true;
        for (Task task : mission.getTasks()) {
            if (!task.isCompleted()) {
                allCompleted = false;
                break;
            }
        }
        mission.setStatus(allCompleted ? MissionStatus.SUCCESS : MissionStatus.FAILED);
    }

    // =========================
    // COMMUNICATION LOGIC
    // =========================

    public void requestCommunication(int playerIndex) {
        // Cancel active communication selection
        if (phase == GamePhase.COMMUNICATION && communicationPlayerIndex == playerIndex) {
            phase = GamePhase.TRICKING;
            communicationPlayerIndex = -1;
            communicationRequested[playerIndex] = false;
            return;
        }

        if (phase != GamePhase.TRICKING) return;
        if (getCurrentMission().hasPlayerUsedToken(playerIndex)) return;

        boolean trickHasPlays = !trickManager.getCurrentTrick().getPlays().isEmpty();
        if (!trickHasPlays) {
            phase = GamePhase.COMMUNICATION;
            communicationPlayerIndex = playerIndex;
        } else {
            communicationRequested[playerIndex] = !communicationRequested[playerIndex];
        }
    }

    public int getCommunicationPlayerIndex() {
        return communicationPlayerIndex;
    }

    public boolean isCommunicationRequested(int playerIndex) {
        return communicationRequested[playerIndex];
    }

    public List<Card> getValidCommunicationCards(int playerIndex) {
        if (getCurrentMission().hasPlayerUsedToken(playerIndex)) {
            return new ArrayList<>();
        }
        // A player can only initiate communication if they haven't played their card in the current trick
        if (trickManager.getCurrentTrick().getPlayerPlay(playerIndex) != null) {
            return new ArrayList<>();
        }

        List<Card> hand = players.get(playerIndex).getHand();
        List<Card> validCards = new ArrayList<>();

        for (Card card : hand) {
            if (card.isTrump()) continue;

            List<Card> sameColorCards = new ArrayList<>();
            for (Card c : hand) {
                if (c.getColor() == card.getColor()) {
                    sameColorCards.add(c);
                }
            }

            if (sameColorCards.size() == 1) {
                validCards.add(card);
            } else {
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (Card c : sameColorCards) {
                    if (c.getValue() < min) min = c.getValue();
                    if (c.getValue() > max) max = c.getValue();
                }
                if (card.getValue() == min || card.getValue() == max) {
                    validCards.add(card);
                }
            }
        }
        return validCards;
    }

    public List<TokenPosition> getValidPositionsForCard(int playerIndex, Card card) {
        List<TokenPosition> positions = new ArrayList<>();
        List<Card> hand = players.get(playerIndex).getHand();
        List<Card> sameColorCards = new ArrayList<>();
        for (Card c : hand) {
            if (c.getColor() == card.getColor()) {
                sameColorCards.add(c);
            }
        }

        if (sameColorCards.size() == 1) {
            positions.add(TokenPosition.MIDDLE);
        } else {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (Card c : sameColorCards) {
                if (c.getValue() < min) min = c.getValue();
                if (c.getValue() > max) max = c.getValue();
            }
            if (card.getValue() == min) positions.add(TokenPosition.BOTTOM);
            if (card.getValue() == max) positions.add(TokenPosition.TOP);
        }
        return positions;
    }

    public TokenPosition resolveCommunicationPosition(int playerIndex, Card card) {
        List<TokenPosition> positions = getValidPositionsForCard(playerIndex, card);
        if (positions.isEmpty()) return null;
        // MIDDLE if only card of that color; TOP if highest; BOTTOM if lowest
        if (positions.contains(TokenPosition.MIDDLE)) return TokenPosition.MIDDLE;
        return positions.get(0);
    }

    public boolean selectCommunicationCard(int playerIndex, Card card, TokenPosition position) {
        if (phase != GamePhase.COMMUNICATION) {
            return false;
        }
        if (playerIndex != communicationPlayerIndex) {
            return false;
        }
        if (getCurrentMission().hasPlayerUsedToken(playerIndex)) {
            return false;
        }
        if (trickManager.getCurrentTrick().getPlayerPlay(playerIndex) != null) {
            return false;
        }
        // Validation of card and position
        List<Card> validCards = getValidCommunicationCards(playerIndex);
        if (!validCards.contains(card)) {
            return false;
        }
        List<TokenPosition> validPositions = getValidPositionsForCard(playerIndex, card);
        if (!validPositions.contains(position)) {
            return false;
        }

        pendingTokens[playerIndex] = new CommunicationToken(card, position, playerIndex);
        getCurrentMission().setPlayerUsedToken(playerIndex, true);

        communicationRequested[playerIndex] = false;
        communicationPlayerIndex = -1;
        phase = GamePhase.TRICKING;
        return true;
    }

    public void applyPendingTokens() {
        if (pendingTokens == null) return;
        for (int i = 0; i < pendingTokens.length; i++) {
            if (pendingTokens[i] != null) {
                getCurrentMission().addActiveToken(pendingTokens[i]);
                pendingTokens[i] = null;
            }
        }
    }

    public CommunicationToken[] getPendingTokens() {
        return pendingTokens;
    }

    // =========================
    // STATE ACCESSORS & HELPERS
    // =========================

    public void removeActiveToken(int playerIndex) {
        getCurrentMission().removeActiveToken(playerIndex);
    }

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