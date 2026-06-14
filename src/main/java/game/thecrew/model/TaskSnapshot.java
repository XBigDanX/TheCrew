package game.thecrew.model;

import java.io.Serializable;

public class TaskSnapshot implements Serializable {

    public int taskIndex;
    public Integer assignedPlayer;
    public boolean completed;

    public TaskSnapshot(int taskIndex, Integer assignedPlayer, boolean completed) {
        this.taskIndex = taskIndex;
        this.assignedPlayer = assignedPlayer;
        this.completed = completed;
    }
}
