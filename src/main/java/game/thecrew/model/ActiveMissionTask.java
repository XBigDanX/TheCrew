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

    public void checkTrick(Mission mission, Trick trick, int winner) {
        if (completed || assignedPlayer == null || !task.getRule().isTrickBased()) return;

        boolean success = task.getRule().checkTrick(mission, trick, winner);

        if (success && assignedPlayer == winner) {
            markCompleted();
        }
    }

    public void checkMissionEnd(Mission mission) {
        if (completed || assignedPlayer == null) return;

        if (!task.getRule().isTrickBased()) {
            if (task.getRule().checkMissionEnd(mission, assignedPlayer)) {
                markCompleted();
            }
        }
    }

    public void markCompleted() {
        this.completed = true;
    }
}
