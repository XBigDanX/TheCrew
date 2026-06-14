package game.thecrew.controllers;

import game.thecrew.GameSession;
import game.thecrew.utils.FileUtils;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import game.thecrew.model.*;
import game.thecrew.ui.CardView;
import game.thecrew.ui.PlayerUI;
import game.thecrew.ui.TaskView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
    @FXML private Button saveButton;
    @FXML private Button loadButton;

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

        passTaskSelectionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                onPassClicked();
            }
        });
        initCommButtons();

        nextMissionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                session.getEngine().advanceToNextMission();
                refreshAfterMissionTransition();
            }
        });
        retryButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                session.getEngine().restartCurrentMission();
                refreshAfterMissionTransition();
            }
        });

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                onSaveClicked();
            }
        });
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                onLoadClicked();
            }
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
            Button communicationButton = new Button();
            communicationButton.setMinSize(30, 30);
            communicationButton.setMaxSize(30, 30);
            communicationButton.setShape(new javafx.scene.shape.Circle(15));
            communicationButton.setStyle("-fx-background-color: green; -fx-cursor: hand;");
            communicationButton.setManaged(false);
            communicationButton.setVisible(false);
            final int index = i;
            communicationButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    onCommunicateClicked(index);
                }
            });
            StackPane.setAlignment(communicationButton, javafx.geometry.Pos.CENTER);
            playerUIs.get(i).getCommunicationArea().getChildren().add(communicationButton);
            commActionButtons[i] = communicationButton;
        }
    }

    private void setupPlayerViews() {
        for (int i = 0; i < playerUIs.size(); i++) {
            boolean active = i < playerCount;
            playerUIs.get(i).setVisible(active);
            if (i < infoLabels.length && infoLabels[i] != null) {
                infoLabels[i].setVisible(active);
            }
        }
    }

    private void updateInfoLabels() {
        Mission mission = session.getEngine().getCurrentMission();
        for (int i = 0; i < playerCount; i++) {
            int tricks = mission.getPlayerWinCount(i);
            int cards = session.getEngine().getPlayerManager().getPlayers().get(i).getHand().size();
            String captain = i == mission.getCaptainIndex() ? "  [Captain]" : "";
            if (i < infoLabels.length && infoLabels[i] != null) {
                infoLabels[i].setText("Tricks: " + tricks + "  Cards: " + cards + captain);
            }
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
        Player player = session.getEngine().getPlayerManager().getPlayers().get(playerIndex);
        FlowPane handPane = playerUIs.get(playerIndex).getHand();
        handPane.getChildren().clear();

        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
        List<Card> validCommCards = (communicatingPlayerIndex == playerIndex)
                ? session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, session.getEngine().getCurrentMission())
                : null;

        for (Card card : player.getHand()) {
            CardView cardView = new CardView(card);

            if (validCommCards != null && validCommCards.contains(card)) {
                final Card clickedCard = card;
                cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                cardView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        onCommunicationCardSelected(playerIndex, clickedCard);
                    }
                });
            } else if (communicatingPlayerIndex == -1) {
                final Card clickedCard = card;
                cardView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        onCardClicked(playerIndex, clickedCard);
                    }
                });
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
                final Task clickedTask = activeTask;
                taskView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        onTaskClicked(playerIndexFromTurn(), clickedTask);
                    }
                });
                availableTasksBox.getChildren().add(taskView);
            }
        }
    }

    // =========================
    // EVENT HANDLERS
    // =========================

    private void onPassClicked() {
        int playerIndex = session.getEngine().getPlayerManager().getCurrentPlayerIndex();
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
            javafx.animation.PauseTransition pauseTransition = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pauseTransition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    clearTrickSlots();
                    renderAllHands();
                    updateInfoLabels();
                    updateCommunicationUI();
                    handleMissionEnd();
                }
            });
            pauseTransition.play();
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
        TokenPosition position = session.getEngine().getCommunicationManager().resolveCommunicationPosition(playerIndex, card);
        if (position == null) return;

        if (session.getEngine().selectCommunicationCard(playerIndex, card, position)) {
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

            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(5));
            pauseTransition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    session.getEngine().removeActiveToken(playerIndex);
                    updateCommunicationUI();
                }
            });
            pauseTransition.play();
        }
    }

    private void updateCommunicationUI() {
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        boolean phaseIsComm = session.getEngine().getPhase() == GamePhase.COMMUNICATION;
        boolean phaseIsTricking = session.getEngine().getPhase() == GamePhase.TRICKING;
        boolean showButtons = phaseIsComm || phaseIsTricking;
        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();

        for (int i = 0; i < playerCount; i++) {
            Pane commArea = playerUIs.get(i).getCommunicationArea();
            commArea.getChildren().clear();

            Button communicationButton = commActionButtons[i];
            commArea.getChildren().add(communicationButton);

            communicationButton.setManaged(showButtons);
            communicationButton.setVisible(showButtons);
            if (showButtons) {
                boolean alreadyUsed = mission.hasPlayerUsedToken(i);
                if (alreadyUsed) {
                    communicationButton.setStyle("-fx-background-color: red; -fx-cursor: default;");
                    communicationButton.setDisable(true);
                } else if (phaseIsComm) {
                    if (communicatingPlayerIndex == i) {
                        communicationButton.setStyle("-fx-background-color: red; -fx-cursor: hand;");
                        communicationButton.setDisable(false);
                    } else if (communicatingPlayerIndex == -1) {
                        boolean canCommunicate = !alreadyUsed &&
                                          !session.getEngine().getCommunicationManager().getValidCommunicationCards(i, mission).isEmpty();
                        communicationButton.setStyle(canCommunicate
                            ? "-fx-background-color: green; -fx-cursor: hand;"
                            : "-fx-background-color: grey; -fx-cursor: default;");
                        communicationButton.setDisable(!canCommunicate);
                    } else {
                        communicationButton.setDisable(true);
                    }
                } else {
                    // TRICKING phase — show queue state
                    if (session.getEngine().getCommunicationManager().isCommunicationRequested(i)) {
                        communicationButton.setStyle("-fx-background-color: orange; -fx-cursor: hand;");
                        communicationButton.setDisable(false);
                    } else {
                        communicationButton.setStyle("-fx-background-color: green; -fx-cursor: hand;");
                        communicationButton.setDisable(false);
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
        for (PlayerUI playerUI : playerUIs) {
            playerUI.getSlot().getChildren().clear();
        }
    }

    private void updateTaskUI() {
        for (int i = 0; i < playerCount; i++) {
            HBox taskHand = playerUIs.get(i).getTaskHand();
            taskHand.getChildren().clear();

            Player player = session.getEngine().getPlayerManager().getPlayers().get(i);
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
            "Current Turn: Player " + (session.getEngine().getPlayerManager().getCurrentPlayerIndex()+1)
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

    private void onSaveClicked() {
        GameState state = session.getEngine().saveState();
        FileUtils.save(state);
    }

    private void onLoadClicked() {
        GameState state = FileUtils.load();
        if (state != null) {
            session.getEngine().restoreState(state);
            refreshAfterMissionTransition();
        }
    }

    private void refreshAfterMissionTransition() {
        missionResultOverlay.setVisible(false);
        missionResultOverlay.setManaged(false);
        clearTrickSlots();
        renderAllHands();
        renderCurrentTrick();
        updateInfoLabels();
        renderTasks();
        updateTaskUI();
        updateMissionLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
        updateCommunicationUI();
    }

    private void renderCurrentTrick() {
        Trick currentTrick = session.getEngine().getTrickManager().getCurrentTrick();
        if (currentTrick == null) return;
        for (TrickPlay trickPlay : currentTrick.getPlays()) {
            int playerIndex = trickPlay.getPlayerIndex();
            if (playerIndex >= 0 && playerIndex < playerUIs.size()) {
                Pane slot = playerUIs.get(playerIndex).getSlot();
                slot.getChildren().clear();
                slot.getChildren().add(new CardView(trickPlay.getCard()));
            }
        }
    }

    private int playerIndexFromTurn() {
        return session.getEngine().getPlayerManager().getCurrentPlayerIndex();
    }
}