package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.GamePhase;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;
import game.thecrew.model.Task;
import game.thecrew.ui.TaskView;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.BiConsumer;

public class TaskUIManager {

    private final HBox availableTasksBox;
    private final HBox[] taskHands;

    public TaskUIManager(HBox availableTasksBox, HBox taskHand0, HBox taskHand1, HBox taskHand2, HBox taskHand3, HBox taskHand4) {
        this.availableTasksBox = availableTasksBox;
        taskHands = new HBox[]{taskHand0, taskHand1, taskHand2, taskHand3, taskHand4};
    }

    public void renderTasks(GameSession session, int playerCount, BiConsumer<Integer, Task> onTaskClicked) {
        if (availableTasksBox == null || session == null || session.getEngine() == null) return;
        availableTasksBox.getChildren().clear();
        Mission mission = session.getEngine().getCurrentMission();

        if (mission == null) return;

        boolean isMyTurn = GameApplication.playerInfo != null &&
            session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.playerInfo.getIndex();

        for (Task activeTask : mission.getTasks()) {
            if (activeTask.getAssignedPlayer() == null) {
                TaskView taskView = new TaskView(activeTask);
                if (isMyTurn && session.getEngine().getPhase() == GamePhase.TASK_SELECTION) {
                    Task clickedTask = activeTask;
                    taskView.setOnMouseClicked(e -> onTaskClicked.accept(playerIndexFromTurn(session), clickedTask));
                }
                availableTasksBox.getChildren().add(taskView);
            }
        }
    }

    public void updateTaskUI(GameSession session, int playerCount) {
        if (session == null || session.getEngine() == null) return;
        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        for (int i = 0; i < playerCount; i++) {
            if (i >= taskHands.length || taskHands[i] == null) continue;
            taskHands[i].getChildren().clear();

            if (i >= players.size()) continue;

            Player player = players.get(i);
            for (Task activeTask : player.getTaskHand()) {
                TaskView taskView = new TaskView(activeTask);
                taskView.setCompleted(activeTask.isCompleted());
                taskHands[i].getChildren().add(taskView);
            }
        }
    }

    private int playerIndexFromTurn(GameSession session) {
        return session.getEngine().getPlayerManager().getCurrentPlayerIndex();
    }
}
