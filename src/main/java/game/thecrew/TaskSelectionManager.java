package game.thecrew;

import game.thecrew.model.ActiveMissionTask;
import game.thecrew.model.Player;
import java.util.List;

public class TaskSelectionManager {
    private final List<Player> players;
    private final List<ActiveMissionTask> availableTasks;
    private int playersProcessed;

    public TaskSelectionManager(List<Player> players, List<ActiveMissionTask> tasks) {
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

    public void selectTask(int playerIndex, ActiveMissionTask task) {
        Player player = players.get(playerIndex);
        task.assignPlayer(playerIndex);
        player.addTask(task);
        availableTasks.remove(task);
        playersProcessed++;
    }

    public void pass() {
        playersProcessed++;
    }

    public boolean isSelectionFinished() {
        return availableTasks.isEmpty();
    }
}
