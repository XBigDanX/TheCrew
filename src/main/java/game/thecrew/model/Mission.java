package game.thecrew.model;

import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final int id;
    private final int difficulty;
    private final List<ActiveMissionTask> tasks;
    private final List<Trick> completedTricks = new ArrayList<>();
    private MissionStatus status = MissionStatus.IN_PROGRESS;

    public Mission(int id, int difficulty, List<ActiveMissionTask> tasks) {
        this.id = id;
        this.difficulty = difficulty;
        this.tasks = tasks;
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

    public List<ActiveMissionTask> getTasks() {
        return tasks;
    }

    public List<Trick> getCompletedTricks() {
        return completedTricks;
    }

    public void addCompletedTrick(Trick trick) {
        completedTricks.add(trick);
        int winner = trick.getWinnerIndex(trick.getLeadSuit());
        for (ActiveMissionTask task : tasks) {
            task.checkTrick(this, trick, winner);
        }
    }

    public int getCompletedTricksCount() {
        return completedTricks.size();
    }

    public int getTrickWinnerIndex(int trickIndex) {
        Trick trick = completedTricks.get(trickIndex);
        return trick.getWinnerIndex(trick.getLeadSuit());
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
}