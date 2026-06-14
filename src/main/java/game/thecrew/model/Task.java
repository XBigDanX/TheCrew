package game.thecrew.model;

import java.io.Serializable;

public class Task implements Serializable {

    private final String description;
    private final TaskRule rule;
    private Integer assignedPlayer;
    private boolean completed;

    public Task(String description, TaskRule rule) {
        this.description = description;
        this.rule = rule;
    }

    public String getDescription() {
        return description;
    }

    public TaskRule getRule() {
        return rule;
    }

    public Integer getAssignedPlayer() {
        return assignedPlayer;
    }

    public void assignPlayer(int playerIndex) {
        this.assignedPlayer = playerIndex;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void checkTrick(Mission mission, Trick trick, int winner) {
        if (completed || assignedPlayer == null || !rule.isTrickBased()) return;

        boolean success = rule.checkTrick(mission, trick, winner);

        if (success && assignedPlayer == winner) {
            markCompleted();
        }
    }

    public void checkMissionEnd(Mission mission) {
        if (completed || assignedPlayer == null) return;

        if (!rule.isTrickBased()) {
            if (rule.checkMissionEnd(mission, assignedPlayer)) {
                markCompleted();
            }
        }
    }
}
