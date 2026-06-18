package game.thecrew.model;

import game.thecrew.network.rmi.MissionService;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mission implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(Mission.class.getName());

    private final int id;
    private final int difficulty;
    private final String description;
    private final List<Task> tasks;
    private final int playerCount;
    private int captainIndex;
    private final List<Trick> completedTricks = new ArrayList<>();
    private final boolean[] communicationTokenUsed;
    private final List<CommunicationToken> activeTokens = new ArrayList<>();
    private MissionStatus status = MissionStatus.IN_PROGRESS;
    private transient MissionService missionService;

    public MissionService getMissionService() {
        return missionService;
    }

    public void setMissionService(MissionService missionService) {
        this.missionService = missionService;
    }

    public Mission(int id, int difficulty, String description, List<Task> tasks, int playerCount) {
        this.id = id;
        this.difficulty = difficulty;
        this.description = description;
        this.tasks = tasks;
        this.playerCount = playerCount;
        this.communicationTokenUsed = new boolean[playerCount];
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getCaptainIndex() {
        return captainIndex;
    }

    public void setCaptainIndex(int captainIndex) {
        this.captainIndex = captainIndex;
    }

    public List<Trick> getCompletedTricks() {
        return completedTricks;
    }

    public void addCompletedTrick(Trick trick) {
        completedTricks.add(trick);
        int winner = trick.getWinnerIndex(trick.getLeadSuit());
        for (Task task : tasks) {
            task.checkTrick(this, trick, winner);
        }
    }

    public int getCompletedTricksCount() {
        return completedTricks.size();
    }

    public int getPlayerWinCount(int playerIndex) {
        int count = 0;
        for (Trick trick : completedTricks) {
            if (trick.getWinnerIndex(trick.getLeadSuit()) == playerIndex) {
                count++;
            }
        }
        return count;
    }

    public List<Card> getCardsWonByPlayer(int playerIndex) {
        List<Card> cards = new ArrayList<>();
        for (Trick trick : completedTricks) {
            if (trick.getWinnerIndex(trick.getLeadSuit()) == playerIndex) {
                for (TrickPlay play : trick.getPlays()) {
                    cards.add(play.getCard());
                }
            }
        }
        return cards;
    }

    public boolean hasPlayerUsedToken(int playerIndex) {
        return communicationTokenUsed[playerIndex];
    }

    public void setPlayerUsedToken(int playerIndex, boolean used) {
        communicationTokenUsed[playerIndex] = used;
    }

    public List<CommunicationToken> getActiveTokens() {
        return activeTokens;
    }

    public void addActiveToken(CommunicationToken token) {
        activeTokens.add(token);
    }

    public void removeActiveToken(int playerIndex) {
        Iterator<CommunicationToken> it = activeTokens.iterator();
        while (it.hasNext()) {
            if (it.next().getPlayerIndex() == playerIndex) {
                it.remove();
            }
        }
    }

    public boolean areAllTasksCompleted() {
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    public void evaluateEnd() {
        for (Task task : tasks) {
            task.checkMissionEnd(this);
        }
        status = areAllTasksCompleted() ? MissionStatus.SUCCESS : MissionStatus.FAILED;
        if (missionService != null) {
            try {
                missionService.logMissionCompletion(id, status == MissionStatus.SUCCESS, "Server");
            } catch (RemoteException e) {
                LOGGER.log(Level.WARNING, "[RMI] Failed to log mission completion: {0}", e.getMessage());
            }
        }
    }
}