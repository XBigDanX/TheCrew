package game.thecrew.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {

    private int missionId;
    private MissionStatus status;
    private int currentMissionNumber;
    private GamePhase phase;
    private int currentPlayerIndex;
    private int captainIndex;
    private int playersProcessed;

    private Map<Integer, List<Card>> playerHands;

    private List<TaskSnapshot> tasks;

    private boolean[] communicationUsed;
    private int communicationPlayerIndex = -1;
    private boolean[] communicationRequested;
    private List<CommunicationToken> activeTokens;

    private List<Trick> completedTricks;
    private Trick currentTrick;

    public GameState() {
        // empty
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public int getCurrentMissionNumber() {
        return currentMissionNumber;
    }

    public void setCurrentMissionNumber(int currentMissionNumber) {
        this.currentMissionNumber = currentMissionNumber;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getCaptainIndex() {
        return captainIndex;
    }

    public void setCaptainIndex(int captainIndex) {
        this.captainIndex = captainIndex;
    }

    public int getPlayersProcessed() {
        return playersProcessed;
    }

    public void setPlayersProcessed(int playersProcessed) {
        this.playersProcessed = playersProcessed;
    }

    public Map<Integer, List<Card>> getPlayerHands() {
        return playerHands;
    }

    public void setPlayerHands(Map<Integer, List<Card>> playerHands) {
        this.playerHands = playerHands;
    }

    public List<TaskSnapshot> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskSnapshot> tasks) {
        this.tasks = tasks;
    }

    public boolean[] getCommunicationUsed() {
        return communicationUsed;
    }

    public void setCommunicationUsed(boolean[] communicationUsed) {
        this.communicationUsed = communicationUsed;
    }

    public int getCommunicationPlayerIndex() {
        return communicationPlayerIndex;
    }

    public void setCommunicationPlayerIndex(int communicationPlayerIndex) {
        this.communicationPlayerIndex = communicationPlayerIndex;
    }

    public boolean[] getCommunicationRequested() {
        return communicationRequested;
    }

    public void setCommunicationRequested(boolean[] communicationRequested) {
        this.communicationRequested = communicationRequested;
    }

    public List<CommunicationToken> getActiveTokens() {
        return activeTokens;
    }

    public void setActiveTokens(List<CommunicationToken> activeTokens) {
        this.activeTokens = activeTokens;
    }

    public List<Trick> getCompletedTricks() {
        return completedTricks;
    }

    public void setCompletedTricks(List<Trick> completedTricks) {
        this.completedTricks = completedTricks;
    }

    public Trick getCurrentTrick() {
        return currentTrick;
    }

    public void setCurrentTrick(Trick currentTrick) {
        this.currentTrick = currentTrick;
    }
}
