package game.thecrew.model;

public class ActiveMissionTask {

    private final Task task;
    private Integer assignedPlayer;
    private boolean completed;

    public ActiveMissionTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
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

    public boolean canComplete() {
        return true;
    }

    public void markCompleted() {
        this.completed = true;
    }
}
