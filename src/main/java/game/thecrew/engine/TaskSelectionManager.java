package game.thecrew.engine;

import game.thecrew.model.Player;
import game.thecrew.model.Task;
import java.util.List;

public class TaskSelectionManager {
    private final List<Player> players;
    private final List<Task> availableTasks;
    private int playersProcessed;

    public TaskSelectionManager(List<Player> players, List<Task> tasks) {
        this.players = players;
        this.availableTasks = tasks;
        this.playersProcessed = 0;
    }

    public boolean canSkip(int playerIndex, int currentPlayerIndex) {
        if (playerIndex != currentPlayerIndex) return false;

        int taskCount = availableTasks.size();
        int playerCount = players.size();

        if (taskCount >= playerCount) {
            return false;
        }

        int remainingPlayersInLoop = playerCount - playersProcessed;
        return remainingPlayersInLoop > taskCount;
    }

    public void selectTask(int playerIndex, Task task) {
        Player player = players.get(playerIndex);
        task.assignPlayer(playerIndex);
        player.addTask(task);
        playersProcessed++;
    }

    public void pass() {
        playersProcessed++;
    }

    public boolean isSelectionFinished() {
        for (Task task : availableTasks) {
            if (task.getAssignedPlayer() == null) {
                return false;
            }
        }
        return true;
    }
}
