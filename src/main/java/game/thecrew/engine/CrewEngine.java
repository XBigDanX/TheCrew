package game.thecrew.engine;

import game.thecrew.mission.MissionLibrary;
import game.thecrew.model.*;

import java.util.ArrayList;
import java.util.List;

public class CrewEngine {

    final List<Mission> missions = new ArrayList<>();
    int currentMissionIndex = 0;

    GamePhase phase = GamePhase.TASK_SELECTION;

    int cardsPlayedInMission = 0;

    private final CardManager cardManager = new CardManager();
    final PlayerManager playerManager = new PlayerManager(cardManager);
    TaskSelectionManager taskManager;
    private final TrickManager trickManager = new TrickManager();
    CommunicationManager communicationManager;
    GameStateManager stateManager;

    public void createPlayers(int playerCount) {
        if (playerManager.getPlayerCount() == playerCount && stateManager != null) {
            return; // Already initialized correctly
        }
        playerManager.createPlayers(playerCount);
        communicationManager = new CommunicationManager(playerManager.getPlayers(), trickManager);
        communicationManager.init(playerCount);
        stateManager = new GameStateManager(this);
        taskManager = new TaskSelectionManager(playerManager.getPlayers(), new ArrayList<>());
    }

    public void dealCards() {
        playerManager.dealCards();
    }

    public void startGame() {
        missions.clear();
        int playerCount = playerManager.getPlayerCount();
        playerManager.setCaptainIndex(cardManager.determineCaptain(playerManager.getPlayers()));
        for (int id = 1; id <= 32; id++) {
            Mission mission = MissionLibrary.forPlayerCount(id, playerCount);
            mission.setCaptainIndex(playerManager.getCaptainIndex());
            missions.add(mission);
        }
        startNewMission();
    }

    private void startNewMission() {
        cardsPlayedInMission = 0;
        playerManager.clearHands();
        playerManager.dealCards();
        playerManager.setCaptainIndex(cardManager.determineCaptain(playerManager.getPlayers()));
        startTaskSelectionPhase();
    }

    private void startTaskSelectionPhase() {
        phase = GamePhase.TASK_SELECTION;
        playerManager.setCurrentPlayerIndex(playerManager.getCaptainIndex());
        taskManager = new TaskSelectionManager(playerManager.getPlayers(), getCurrentMission().getTasks());
    }

    public boolean selectTask(int playerIndex, Task task) {
        if (phase != GamePhase.TASK_SELECTION || taskManager == null || playerIndex != playerManager.getCurrentPlayerIndex()) return false;
        
        // Find the matching task in the current mission (because the task passed is a deserialized copy)
        Task matchingTask = null;
        for (Task t : getCurrentMission().getTasks()) {
            if (t.equals(task) && t.getAssignedPlayer() == null) {
                matchingTask = t;
                break;
            }
        }
        
        if (matchingTask == null) return false;
        
        taskManager.selectTask(playerIndex, matchingTask);
        if (taskManager.isSelectionFinished()) startTrickPhase();
        else playerManager.nextPlayer();
        return true;
    }

    public boolean passTaskSelection(int playerIndex) {
        if (phase != GamePhase.TASK_SELECTION || taskManager == null || !taskManager.canSkip(playerIndex, playerManager.getCurrentPlayerIndex())) return false;
        taskManager.pass();
        playerManager.nextPlayer();
        return true;
    }

    private void startTrickPhase() {
        int requestingPlayerIndex = communicationManager.getNextRequestPlayerIndex();
        if (requestingPlayerIndex >= 0) {
            phase = GamePhase.COMMUNICATION;
            communicationManager.startCommunication(requestingPlayerIndex);
        } else {
            phase = GamePhase.TRICKING;
            playerManager.setCurrentPlayerIndex(playerManager.getCaptainIndex());
            communicationManager.applyPendingTokens(getCurrentMission());
            trickManager.reset();
        }
    }

    public void requestCommunication(int playerIndex) {
        Mission mission = getCurrentMission();
        if (mission == null || (phase != GamePhase.TRICKING && phase != GamePhase.COMMUNICATION)) return;
        if (phase == GamePhase.COMMUNICATION && communicationManager.getCommunicationPlayerIndex() == playerIndex) {
            communicationManager.cancelCommunication(playerIndex);
            phase = GamePhase.TRICKING;
        } else if (phase == GamePhase.TRICKING && !mission.hasPlayerUsedToken(playerIndex)) {
            if (trickManager.getCurrentTrick().getPlays().isEmpty()) {
                communicationManager.startCommunication(playerIndex);
                phase = GamePhase.COMMUNICATION;
            } else communicationManager.toggleRequest(playerIndex);
        }
    }

    public boolean selectCommunicationCard(int playerIndex, Card card, TokenPosition position) {
        if (phase != GamePhase.COMMUNICATION || !communicationManager.selectCommunicationCard(playerIndex, card, position, getCurrentMission())) return false;
        phase = GamePhase.TRICKING;
        return true;
    }

    public void applyPendingTokens() {
        communicationManager.applyPendingTokens(getCurrentMission());
    }

    public void removeActiveToken(int playerIndex) {
        communicationManager.removeActiveToken(playerIndex, getCurrentMission());
    }

    public boolean playCard(int playerIndex, Card card) {
        if (phase != GamePhase.TRICKING || playerIndex != playerManager.getCurrentPlayerIndex()) return false;
        Player player = playerManager.getPlayers().get(playerIndex);
        
        // Find the actual card object in the player's hand
        Card actualCard = null;
        for (Card c : player.getHand()) {
            if (c.equals(card)) {
                actualCard = c;
                break;
            }
        }
        
        if (actualCard == null || !trickManager.playCard(playerIndex, actualCard, player.getHand())) return false;
        player.removeCardFromHand(actualCard);
        cardsPlayedInMission++;
        if (trickManager.isComplete(playerManager.getPlayerCount())) handleCompletedTrick();
        else playerManager.nextPlayer();
        return true;
    }

    public void handleCompletedTrick() {
        Trick completed = trickManager.getCurrentTrick();
        getCurrentMission().addCompletedTrick(completed);
        if (getCurrentMission().areAllTasksCompleted()) {
            getCurrentMission().evaluateEnd();
            phase = GamePhase.MISSION_COMPLETE;
        } else {
            playerManager.setCurrentPlayerIndex(trickManager.getWinner());
            communicationManager.applyPendingTokens(getCurrentMission());
            int playerCount = playerManager.getPlayerCount();
            int maxCardsBeforeEnd = playerCount == 2 ? 40 : playerCount == 3 ? 39 : 40;
            if (cardsPlayedInMission >= maxCardsBeforeEnd) {
                getCurrentMission().evaluateEnd();
                phase = getCurrentMission().getStatus() == MissionStatus.SUCCESS ? GamePhase.MISSION_COMPLETE : GamePhase.GAME_OVER;
            } else if (communicationManager.getNextRequestPlayerIndex() >= 0) {
                startTrickPhase();
            }
        }
    }

    public void resetTrick() {
        trickManager.reset();
    }

    public void nextMission() {
        if (currentMissionIndex < missions.size() - 1) {
            currentMissionIndex++;
            communicationManager.reset();
            startNewMission();
        }
    }

    public void advanceToNextMission() {
        nextMission();
    }

    public void restartCurrentMission() {
        Mission current = getCurrentMission();
        Mission fresh = MissionLibrary.forPlayerCount(current.getId(), playerManager.getPlayerCount());
        fresh.setCaptainIndex(playerManager.getCaptainIndex());
        missions.set(currentMissionIndex, fresh);
        communicationManager.reset();
        startNewMission();
    }

    public GameState saveState() {
        return stateManager.saveState();
    }

    public void restoreState(GameState state) {
        stateManager.restoreState(state);
    }

    public Mission getCurrentMission() {
        return missions.isEmpty() ? null : missions.get(currentMissionIndex);
    }

    public int getCurrentMissionNumber() {
        return currentMissionIndex + 1;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public CommunicationManager getCommunicationManager() {
        return communicationManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TrickManager getTrickManager() {
        return trickManager;
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    public CommunicationToken[] getPendingTokens() {
        return communicationManager.getPendingTokens();
    }

}
