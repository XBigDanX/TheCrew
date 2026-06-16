package game.thecrew.model;

import java.io.Serializable;

public class TaskSnapshot implements Serializable {

    private int taskIndex;
    private Integer assignedPlayer;
    private boolean completed;

    public TaskSnapshot(int taskIndex, Integer assignedPlayer, boolean completed) {
        this.taskIndex = taskIndex;
        this.assignedPlayer = assignedPlayer;
        this.completed = completed;
    }

    public int getTaskIndex() {
        return taskIndex;
    }

    public Integer getAssignedPlayer() {
        return assignedPlayer;
    }

    public boolean isCompleted() {
        return completed;
    }
}
