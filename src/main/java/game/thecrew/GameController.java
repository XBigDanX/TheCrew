package game.thecrew;

import game.thecrew.model.*;
import game.thecrew.ui.CardView;
import game.thecrew.ui.PlayerUI;
import game.thecrew.ui.TaskView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class GameController {

    @FXML private Label currentPlayerLabel;
    @FXML private Button passTaskSelectionButton;

    @FXML private Pane taskPane;
    @FXML private HBox availableTasksBox;

    @FXML private Pane trickPane;
    @FXML private Pane slot0,slot1,slot2,slot3,slot4;

    @FXML private FlowPane hand0,hand1,hand2,hand3,hand4;
    @FXML private HBox taskHand0,taskHand1,taskHand2,taskHand3,taskHand4;

    private List<PlayerUI> playerUIs;

    private final CrewEngine engine = new CrewEngine();

    private final int playerCount = 2;

    // =========================
    // INIT
    // =========================

    @FXML
    public void initialize() {

        initPlayerUIs();

        engine.createPlayers(playerCount);

        setupMissions();

        engine.dealCards();

        engine.startGame(); // IMPORTANT

        setupPlayerViews();
        renderAllHands();

        renderTasks();

        passTaskSelectionButton.setOnAction(e -> onPassClicked());

        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    // =========================
    // MISSIONS
    // =========================

    private void setupMissions() {

        List<Task> mission1Tasks = new ArrayList<>();
        mission1Tasks.add(new Task("Task 1"));
        mission1Tasks.add(new Task("Task 2"));
        mission1Tasks.add(new Task("Task 3"));



        List<Task> mission2Tasks = new ArrayList<>();
        mission2Tasks.add(new Task("Task 3"));
        mission2Tasks.add(new Task("Task 4"));

        engine.addMission(new Mission("Mission 1", mission1Tasks));
        engine.addMission(new Mission("Mission 2", mission2Tasks));
    }

    // =========================
    // UI SETUP
    // =========================

    private void initPlayerUIs() {

        playerUIs = List.of(
                new PlayerUI(hand0, slot0, taskHand0),
                new PlayerUI(hand1, slot1, taskHand1),
                new PlayerUI(hand2, slot2, taskHand2),
                new PlayerUI(hand3, slot3, taskHand3),
                new PlayerUI(hand4, slot4, taskHand4)
        );
    }

    private void setupPlayerViews() {

        for (int i = 0; i < playerUIs.size(); i++) {
            playerUIs.get(i).setVisible(i < playerCount);
        }
    }

    // =========================
    // HANDS
    // =========================

    private void renderAllHands() {

        for (int i = 0; i < playerCount; i++) {
            renderPlayerHand(i);
        }
    }

    private void renderPlayerHand(int playerIndex) {

        Player player = engine.getPlayers().get(playerIndex);

        FlowPane handPane = playerUIs.get(playerIndex).getHand();

        handPane.getChildren().clear();

        for (Card card : player.getHand()) {

            CardView cardView = new CardView(card);

            cardView.setOnMouseClicked(e ->
                    onCardClicked(playerIndex, card)
            );

            handPane.getChildren().add(cardView);
        }
    }

    // =========================
    // CARDS
    // =========================

    private void onCardClicked(int playerIndex, Card card) {

        if (!engine.playCard(playerIndex, card)) {
            return;
        }

        renderPlayerHand(playerIndex);

        Pane slot = playerUIs.get(playerIndex).getSlot();

        slot.getChildren().clear();
        slot.getChildren().add(new CardView(card));

        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    // =========================
    // TASKS
    // =========================

    private void renderTasks() {

        availableTasksBox.getChildren().clear();

        Mission mission = engine.getCurrentMission();

        if (mission == null) {
            return;
        }

        for (Task task : mission.getTasks()) {

            TaskView taskView = new TaskView(task);

            taskView.setOnMouseClicked(e ->
                    onTaskClicked(playerIndexFromTurn(), task)
            );

            availableTasksBox.getChildren().add(taskView);
        }
    }

    private void onTaskClicked(int playerIndex, Task task) {

        if (!engine.selectTask(playerIndex, task)) {
            return;
        }

        TaskView completedTask = new TaskView(task);
        completedTask.setCompleted(true);

        playerUIs.get(playerIndex)
                .getTaskHand()
                .getChildren()
                .add(completedTask);

        renderTasks();
        updateCurrentPlayerLabel();

        updatePhasePanels();
    }

    private int playerIndexFromTurn() {
        return engine.getCurrentPlayerIndex();
    }

    // =========================
    // PHASES
    // =========================

    private void showTaskPhase() {

        taskPane.setVisible(true);
        taskPane.setManaged(true);
        passTaskSelectionButton.setVisible(true);
        passTaskSelectionButton.setManaged(true);

        trickPane.setVisible(false);
        trickPane.setManaged(false);
    }

    private void showTrickPhase() {

        taskPane.setVisible(false);
        taskPane.setManaged(false);
        passTaskSelectionButton.setVisible(false);
        passTaskSelectionButton.setManaged(false);

        trickPane.setVisible(true);
        trickPane.setManaged(true);
    }

    private void updatePhasePanels() {

        if (engine.getPhase() == GamePhase.TASK_SELECTION) {
            showTaskPhase();
        } else {
            showTrickPhase();
        }
    }

    private void updateCurrentPlayerLabel() {
        currentPlayerLabel.setText(
                "Current Turn: Player " + (engine.getCurrentPlayerIndex()+1)
        );
    }


    private void onPassClicked() {

        int playerIndex = engine.getCurrentPlayerIndex();

        if (!engine.passTaskSelection(playerIndex)) {
            return;
        }

        renderTasks();
        updateCurrentPlayerLabel();
    }
}