package game.thecrew.controllers;

import game.thecrew.GameSession;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
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
import javafx.scene.layout.StackPane;

import java.util.List;

public class GameController {

    @FXML private Label missionLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label missionDescriptionLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Button passTaskSelectionButton;
    @FXML private Button communicateButton; // reserved for per-window view

    @FXML private Pane taskPane;
    @FXML private HBox availableTasksBox;

    @FXML private Pane trickPane;
    @FXML private Pane slot0,slot1,slot2,slot3,slot4;

    @FXML private FlowPane hand0,hand1,hand2,hand3,hand4;
    @FXML private HBox taskHand0,taskHand1,taskHand2,taskHand3,taskHand4;
    @FXML private StackPane commArea0,commArea1,commArea2,commArea3,commArea4;
    @FXML private Label infoLabel0,infoLabel1,infoLabel2,infoLabel3,infoLabel4;

    @FXML private StackPane missionResultOverlay;
    @FXML private Label resultTitleLabel;
    @FXML private Label resultMessageLabel;
    @FXML private Button nextMissionButton;
    @FXML private Button retryButton;

    private List<PlayerUI> playerUIs;

    private static GameSession pendingSession;

    public static void setSession(GameSession session) {
        pendingSession = session;
    }

    private GameSession session;
    private int playerCount;

    private Label[] infoLabels;

    private Button[] commActionButtons;

    // =========================
    // INIT
    // =========================

    @FXML
    public void initialize() {
        session = pendingSession;
        playerCount = session.getPlayerCount();

        initPlayerUIs();
        setupPlayerViews();
        updateInfoLabels();
        renderAllHands();
        renderTasks();

        passTaskSelectionButton.setOnAction(e -> onPassClicked());
        initCommButtons();

        nextMissionButton.setOnAction(e -> {
            session.getEngine().advanceToNextMission();
            refreshAfterMissionTransition();
        });
        retryButton.setOnAction(e -> {
            session.getEngine().restartCurrentMission();
            refreshAfterMissionTransition();
        });

        updateMissionLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    // =========================
    // SETUP HELPERS
    // =========================

    private void initPlayerUIs() {
        playerUIs = List.of(
                new PlayerUI(hand0, slot0, taskHand0, commArea0),
                new PlayerUI(hand1, slot1, taskHand1, commArea1),
                new PlayerUI(hand2, slot2, taskHand2, commArea2),
                new PlayerUI(hand3, slot3, taskHand3, commArea3),
                new PlayerUI(hand4, slot4, taskHand4, commArea4)
        );
        infoLabels = new Label[]{infoLabel0, infoLabel1, infoLabel2, infoLabel3, infoLabel4};
    }

    private void initCommButtons() {
        commActionButtons = new Button[playerUIs.size()];
        for (int i = 0; i < playerUIs.size(); i++) {
            Button btn = new Button();
            btn.setMinSize(30, 30);
            btn.setMaxSize(30, 30);
            btn.setShape(new javafx.scene.shape.Circle(15));
            btn.setStyle("-fx-background-color: green; -fx-cursor: hand;");
            btn.setManaged(false);
            btn.setVisible(false);
            int idx = i;
            btn.setOnAction(e -> onCommunicateClicked(idx));
            StackPane.setAlignment(btn, javafx.geometry.Pos.CENTER);
            playerUIs.get(i).getCommunicationArea().getChildren().add(btn);
            commActionButtons[i] = btn;
        }
    }

    private void setupPlayerViews() {
        for (int i = 0; i < playerUIs.size(); i++) {
            playerUIs.get(i).setVisible(i < playerCount);
            infoLabels[i].setVisible(i < playerCount && i < 2);
        }
    }

    private void updateInfoLabels() {
        Mission mission = session.getEngine().getCurrentMission();
        for (int i = 0; i < playerCount && i < 2; i++) {
            int tricks = mission.getPlayerWinCount(i);
            int cards = session.getEngine().getPlayers().get(i).getHand().size();
            String captain = i == mission.getCaptainIndex() ? "  [Captain]" : "";
            infoLabels[i].setText("Tricks: " + tricks + "  Cards: " + cards + captain);
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
        Player player = session.getEngine().getPlayers().get(playerIndex);
        FlowPane handPane = playerUIs.get(playerIndex).getHand();
        handPane.getChildren().clear();

        int commPlayerIdx = session.getEngine().getCommunicationPlayerIndex();
        List<Card> validCommCards = (commPlayerIdx == playerIndex)
                ? session.getEngine().getValidCommunicationCards(playerIndex)
                : null;

        for (Card card : player.getHand()) {
            CardView cardView = new CardView(card);

            if (validCommCards != null && validCommCards.contains(card)) {
                cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                cardView.setOnMouseClicked(e -> onCommunicationCardSelected(playerIndex, card));
            } else if (commPlayerIdx == -1) {
                cardView.setOnMouseClicked(e -> onCardClicked(playerIndex, card));
            }

            handPane.getChildren().add(cardView);
        }
    }

    private void renderTasks() {
        availableTasksBox.getChildren().clear();
        Mission mission = session.getEngine().getCurrentMission();

        if (mission == null) {
            return;
        }

        for (Task activeTask : mission.getTasks()) {
            if (activeTask.getAssignedPlayer() == null) {
                TaskView taskView = new TaskView(activeTask);
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
        int playerIndex = session.getEngine().getCurrentPlayerIndex();
        if (!session.getEngine().passTaskSelection(playerIndex)) {
            return;
        }
        renderTasks();
        updateCurrentPlayerLabel();
    }

    private void onTaskClicked(int playerIndex, Task task) {
        if (!session.getEngine().selectTask(playerIndex, task)) {
            return;
        }

        updateTaskUI();
        renderTasks();
        updateInfoLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    private void onCardClicked(int playerIndex, Card card) {
        if (!session.getEngine().playCard(playerIndex, card)) {
            return;
        }

        renderPlayerHand(playerIndex);

        Pane slot = playerUIs.get(playerIndex).getSlot();
        slot.getChildren().clear();
        slot.getChildren().add(new CardView(card));

        // If trick was completed, clear it after a short delay
        if (session.getEngine().getTrickManager().getCurrentTrick().getPlays().isEmpty()) {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(e -> {
                clearTrickSlots();
                renderAllHands();
                updateInfoLabels();
                updateCommunicationUI();
                handleMissionEnd();
            });
            pause.play();
        }

        // update task completion in UI
        updateTaskUI();

        updateCurrentPlayerLabel();
        updatePhasePanels();
    }

    private void onCommunicateClicked(int playerIndex) {
        session.getEngine().requestCommunication(playerIndex);
        renderAllHands();
        updateCommunicationUI();
        updatePhasePanels();
    }

    private void onCommunicationCardSelected(int playerIndex, Card card) {
        TokenPosition pos = session.getEngine().resolveCommunicationPosition(playerIndex, card);
        if (pos == null) return;

        if (session.getEngine().selectCommunicationCard(playerIndex, card, pos)) {
            renderAllHands();
            updateCommunicationUI();
            updatePhasePanels();

            // Automatska primjena tokena (ako motor to već ne radi odmah, ali mi želimo efekt od 5 sekundi)
            // U našem motoru, selectCommunicationCard postavlja phase na TRICKING i čisti pending.
            // Ali mi ga želimo u "active tokens" za prikaz.
            
            // Budući da selectCommunicationCard postavlja pendingTokens[playerIndex] i vraća phase u TRICKING,
            // moramo osigurati da se taj pending token premjesti u active (što se obično događa na početku trika).
            // Za ovaj vizualni efekt, možemo ga odmah "primijeniti" za prikaz.
            
            session.getEngine().applyPendingTokens();
            updateCommunicationUI();

            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                session.getEngine().removeActiveToken(playerIndex);
                updateCommunicationUI();
            });
            pause.play();
        }
    }

    private void updateCommunicationUI() {
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        boolean phaseIsComm = session.getEngine().getPhase() == GamePhase.COMMUNICATION;
        boolean phaseIsTricking = session.getEngine().getPhase() == GamePhase.TRICKING;
        boolean showButtons = phaseIsComm || phaseIsTricking;
        int commPlayerIdx = session.getEngine().getCommunicationPlayerIndex();

        for (int i = 0; i < playerCount; i++) {
            Pane commArea = playerUIs.get(i).getCommunicationArea();
            commArea.getChildren().clear();

            Button btn = commActionButtons[i];
            commArea.getChildren().add(btn);

            btn.setManaged(showButtons);
            btn.setVisible(showButtons);
            if (showButtons) {
                boolean alreadyUsed = mission.hasPlayerUsedToken(i);
                if (alreadyUsed) {
                    btn.setStyle("-fx-background-color: red; -fx-cursor: default;");
                    btn.setDisable(true);
                } else if (phaseIsComm) {
                    if (commPlayerIdx == i) {
                        btn.setStyle("-fx-background-color: red; -fx-cursor: hand;");
                        btn.setDisable(false);
                    } else if (commPlayerIdx == -1) {
                        boolean canComm = !alreadyUsed &&
                                          !session.getEngine().getValidCommunicationCards(i).isEmpty();
                        btn.setStyle(canComm
                            ? "-fx-background-color: green; -fx-cursor: hand;"
                            : "-fx-background-color: grey; -fx-cursor: default;");
                        btn.setDisable(!canComm);
                    } else {
                        btn.setDisable(true);
                    }
                } else {
                    // TRICKING phase — show queue state
                    if (session.getEngine().isCommunicationRequested(i)) {
                        btn.setStyle("-fx-background-color: orange; -fx-cursor: hand;");
                        btn.setDisable(false);
                    } else {
                        btn.setStyle("-fx-background-color: green; -fx-cursor: hand;");
                        btn.setDisable(false);
                    }
                }
            }

            // Show active tokens
            for (CommunicationToken token : mission.getActiveTokens()) {
                if (token.getPlayerIndex() == i) {
                    CardView cv = new CardView(token.getCard());
                    cv.addToken(token.getPosition());
                    commArea.getChildren().add(cv);
                }
            }

            // Show pending tokens
            CommunicationToken[] pending = session.getEngine().getPendingTokens();
            if (pending != null && pending[i] != null) {
                CardView cv = new CardView(pending[i].getCard());
                cv.addToken(pending[i].getPosition());
                cv.setOpacity(0.6);
                commArea.getChildren().add(cv);
            }
        }
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

            Player player = session.getEngine().getPlayers().get(i);
            for (Task activeTask : player.getTaskHand()) {
                TaskView taskView = new TaskView(activeTask);
                taskView.setCompleted(activeTask.isCompleted());
                taskHand.getChildren().add(taskView);
            }
        }
    }

    // =========================
    // UI UPDATES & HELPERS
    // =========================

    private void updateMissionLabels() {
        Mission mission = session.getEngine().getCurrentMission();
        missionLabel.setText("Mission " + session.getEngine().getCurrentMissionNumber());
        difficultyLabel.setText("Difficulty: " + (mission != null ? mission.getDifficulty() : "?"));
        missionDescriptionLabel.setText(mission != null ? mission.getDescription() : "");
    }

    private void updateCurrentPlayerLabel() {
        currentPlayerLabel.setText(
                "Current Turn: Player " + (session.getEngine().getCurrentPlayerIndex()+1)
        );
    }

    private void updatePhasePanels() {
        if (session.getEngine().getPhase() == GamePhase.TASK_SELECTION) {
            showTaskPhase();
        } else {
            showTrickPhase();
        }
        updateCommunicationUI();
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

    private void handleMissionEnd() {
        if (session.getEngine().getPhase() != GamePhase.MISSION_COMPLETE) {
            return;
        }
        boolean success = session.getEngine().getCurrentMission().getStatus() == MissionStatus.SUCCESS;
        if (success) {
            resultTitleLabel.setText("Mission Complete!");
            resultMessageLabel.setText("All tasks were completed.");
            nextMissionButton.setVisible(true);
            nextMissionButton.setManaged(true);
            retryButton.setVisible(false);
            retryButton.setManaged(false);
        } else {
            resultTitleLabel.setText("Mission Failed");
            resultMessageLabel.setText("Not all tasks were completed.");
            nextMissionButton.setVisible(false);
            nextMissionButton.setManaged(false);
            retryButton.setVisible(true);
            retryButton.setManaged(true);
        }
        missionResultOverlay.setVisible(true);
        missionResultOverlay.setManaged(true);
    }

    private void refreshAfterMissionTransition() {
        missionResultOverlay.setVisible(false);
        missionResultOverlay.setManaged(false);
        clearTrickSlots();
        renderAllHands();
        updateInfoLabels();
        renderTasks();
        updateTaskUI();
        updateMissionLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
        updateCommunicationUI();
    }

    private int playerIndexFromTurn() {
        return session.getEngine().getCurrentPlayerIndex();
    }
}