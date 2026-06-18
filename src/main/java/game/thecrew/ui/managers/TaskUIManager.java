package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.GamePhase;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;
import game.thecrew.model.Task;
import game.thecrew.network.NetworkActionSender;
import game.thecrew.ui.TaskView;
import javafx.scene.layout.HBox;

import java.util.List;

public class TaskUIManager {

    private final HBox availableTasksBox;
    private final HBox[] taskHands;

    public TaskUIManager(HBox availableTasksBox, HBox taskHand0, HBox taskHand1, HBox taskHand2, HBox taskHand3, HBox taskHand4) {
        this.availableTasksBox = availableTasksBox;
        taskHands = new HBox[]{taskHand0, taskHand1, taskHand2, taskHand3, taskHand4};
    }

    public void renderTasks(GameSession session, NetworkActionSender actionSender) {
        if (availableTasksBox == null || session == null || session.getEngine() == null) return;
        availableTasksBox.getChildren().clear();
        Mission mission = session.getEngine().getCurrentMission();

        if (mission == null) return;

        boolean isMyTurn = GameApplication.getPlayerInfo() != null &&
            session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.getPlayerInfo().getIndex();

        for (Task activeTask : mission.getTasks()) {
            if (activeTask.getAssignedPlayer() == null) {
                TaskView taskView = new TaskView(activeTask);
                if (isMyTurn && session.getEngine().getPhase() == GamePhase.TASK_SELECTION) {
                    Task clickedTask = activeTask;
                    taskView.setOnMouseClicked(e -> actionSender.selectTask(playerIndexFromTurn(session), clickedTask));
                }
                availableTasksBox.getChildren().add(taskView);
            }
        }
    }

    public void updateTaskUI(GameSession session) {
        if (session == null || session.getEngine() == null) return;
        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        for (int i = 0; i < taskHands.length; i++) {
            if (i >= players.size() || taskHands[i] == null) continue;
            taskHands[i].getChildren().clear();

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
    public void setupVisibility(int playerCount) {
        for (int i = 0; i < taskHands.length; i++) {
            boolean active = i < playerCount;
            if (taskHands[i] != null) {
                taskHands[i].setVisible(active);
                taskHands[i].setManaged(active);
            }
        }
    }
}
