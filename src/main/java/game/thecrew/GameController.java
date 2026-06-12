package game.thecrew;

import game.thecrew.engine.CrewEngine;
import game.thecrew.engine.TrickManager;
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

    private final int playerCount = 4;

    // =========================
    // INIT
    // =========================

    @FXML
    public void initialize() {
        initPlayerUIs();
        engine.createPlayers(playerCount);
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
    // SETUP HELPERS
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
    // RENDERING
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

    private void renderTasks() {
        availableTasksBox.getChildren().clear();
        Mission mission = engine.getCurrentMission();

        if (mission == null) {
            return;
        }

        for (ActiveMissionTask activeTask : mission.getTasks()) {
            if (activeTask.getAssignedPlayer() == null) {
                TaskView taskView = new TaskView(activeTask.getTask());
                taskView.setOnMouseClicked(e ->
                        onTaskClicked(playerIndexFromTurn(), activeTask)
                );
                availableTasksBox.getChildren().add(taskView);
            }
        }
    }

    // =========================
    // EVENT HANDLERS
    // =========================

    private void onPassClicked() {
        int playerIndex = engine.getCurrentPlayerIndex();
        if (!engine.passTaskSelection(playerIndex)) {
            return;
        }
        renderTasks();
        updateCurrentPlayerLabel();
    }

    private void onTaskClicked(int playerIndex, ActiveMissionTask task) {
        if (!engine.selectTask(playerIndex, task)) {
            return;
        }

        updateTaskUI();
        renderTasks();
        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    private void onCardClicked(int playerIndex, Card card) {
        if (!engine.playCard(playerIndex, card)) {
            return;
        }


        renderPlayerHand(playerIndex);

        Pane slot = playerUIs.get(playerIndex).getSlot();
        slot.getChildren().clear();
        slot.getChildren().add(new CardView(card));

        // If trick was completed, clear it after a short delay
        if (engine.getTrickManager().getCurrentTrick().getPlays().isEmpty()) {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(e -> clearTrickSlots());
            pause.play();
        }

        // update task completion in UI
        updateTaskUI();

        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    private void clearTrickSlots() {
        for (PlayerUI ui : playerUIs) {
            ui.getSlot().getChildren().clear();
        }
    }

    private void updateTaskUI() {
        for (int i = 0; i < playerCount; i++) {
            HBox taskHand = playerUIs.get(i).getTaskHand();
            taskHand.getChildren().clear();

            Player player = engine.getPlayers().get(i);
            for (ActiveMissionTask activeTask : player.getTaskHand()) {
                TaskView taskView = new TaskView(activeTask.getTask());
                taskView.setCompleted(activeTask.isCompleted());
                taskHand.getChildren().add(taskView);
            }
        }
    }

    // =========================
    // UI UPDATES & HELPERS
    // =========================

    private void updateCurrentPlayerLabel() {
        currentPlayerLabel.setText(
                "Current Turn: Player " + (engine.getCurrentPlayerIndex()+1)
        );
    }

    private void updatePhasePanels() {
        if (engine.getPhase() == GamePhase.TASK_SELECTION) {
            showTaskPhase();
        } else {
            showTrickPhase();
        }
    }

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

    private int playerIndexFromTurn() {
        return engine.getCurrentPlayerIndex();
    }
}