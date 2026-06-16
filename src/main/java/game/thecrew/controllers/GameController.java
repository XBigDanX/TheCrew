package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.network.GameNetworkClient;
import game.thecrew.utils.FileUtils;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import game.thecrew.model.*;
import game.thecrew.ui.CardView;
import game.thecrew.ui.PlayerUI;
import game.thecrew.ui.managers.HandUIManager;
import game.thecrew.ui.managers.TaskUIManager;
import game.thecrew.ui.managers.TrickUIManager;
import javafx.application.Platform;
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

    @FXML public StackPane lobbyOverlay;
    @FXML public Label lobbyStatusLabel;

    private List<PlayerUI> playerUIs;

    public static GameSession pendingSession;

    public static void setSession(GameSession session) {
        pendingSession = session;
    }

    public GameSession session;
    public int playerCount;

    private GameNetworkClient client;

    private Label[] infoLabels;

    private Button[] commActionButtons;

    private boolean dismissTimerScheduled;

    private HandUIManager handUIManager;
    private TrickUIManager trickUIManager;
    private TaskUIManager taskUIManager;

    // =========================
    // INIT
    // =========================

    @FXML
    public void initialize() {
        try {
            if (pendingSession != null) {
                session = pendingSession;
                playerCount = session.getPlayerCount();
            } else {
                if (GameApplication.playerInfo != null) {
                    this.playerCount = GameApplication.playerInfo.getTotalPlayers();
                    if (this.playerCount > 0) {
                        this.session = new GameSession(this.playerCount);
                    }
                }
            }

            client = new GameNetworkClient();
            if (client.isConnected()) {
                client.listen(this);
            }

            if (session != null) {
                initPlayerUIs();
                handUIManager = new HandUIManager(hand0, hand1, hand2, hand3, hand4);
                trickUIManager = new TrickUIManager(slot0, slot1, slot2, slot3, slot4);
                taskUIManager = new TaskUIManager(availableTasksBox, taskHand0, taskHand1, taskHand2, taskHand3, taskHand4);
                setupPlayerViews();
                updateInfoLabels();
                handUIManager.renderAllHands(session, playerCount, this::onCardClicked, this::onCommunicationCardSelected);
                taskUIManager.renderTasks(session, playerCount, this::onTaskClicked);
                initCommButtons();
                updateMissionLabels();
                updateCurrentPlayerLabel();
                updatePhasePanels();
            }

            if (passTaskSelectionButton != null) {
                passTaskSelectionButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        onPassClicked();
                    }
                });
            }

            if (nextMissionButton != null) {
                nextMissionButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        onNextMissionClicked();
                    }
                });
            }
            if (retryButton != null) {
                retryButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        onRetryMissionClicked();
                    }
                });
            }

            if (saveButton != null) {
                saveButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        onSaveClicked();
                    }
                });
            }
            if (loadButton != null) {
                loadButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        onLoadClicked();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Error in GameController.initialize():");
            e.printStackTrace();
            throw e; // Rethrow to let FXML loader handle it but we've logged it
        }
    }



    public void refreshUI() {
        trickUIManager.clearTrickSlots();
        trickUIManager.renderCurrentTrick(session);
        handUIManager.renderAllHands(session, playerCount, this::onCardClicked, this::onCommunicationCardSelected);
        taskUIManager.renderTasks(session, playerCount, this::onTaskClicked);
        taskUIManager.updateTaskUI(session, playerCount);
        updateInfoLabels();
        updateMissionLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
        handleMissionEnd();
    }

    // =========================
    // SETUP HELPERS
    // =========================

    public void initPlayerUIs() {
        PlayerUI p0 = new PlayerUI(hand0, slot0, taskHand0, commArea0);
        PlayerUI p1 = new PlayerUI(hand1, slot1, taskHand1, commArea1);
        PlayerUI p2 = new PlayerUI(hand2, slot2, taskHand2, commArea2);
        PlayerUI p3 = new PlayerUI(hand3, slot3, taskHand3, commArea3);
        PlayerUI p4 = new PlayerUI(hand4, slot4, taskHand4, commArea4);
        
        playerUIs = new java.util.ArrayList<>();
        playerUIs.add(p0);
        playerUIs.add(p1);
        playerUIs.add(p2);
        playerUIs.add(p3);
        playerUIs.add(p4);

        infoLabels = new Label[]{infoLabel0, infoLabel1, infoLabel2, infoLabel3, infoLabel4};
    }

    private void initCommButtons() {
        if (playerUIs == null) return;
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
            if (playerUIs.get(i).getCommunicationArea() != null) {
                playerUIs.get(i).getCommunicationArea().getChildren().add(communicationButton);
            }
            commActionButtons[i] = communicationButton;
        }
    }

    public void setupPlayerViews() {
        int myIndex = (GameApplication.playerInfo != null) ? GameApplication.playerInfo.getIndex() : -1;
        for (int i = 0; i < playerUIs.size(); i++) {
            boolean isActive = i < playerCount;
            PlayerUI ui = playerUIs.get(i);
            
            // All components except the hand are visible if the player is in the game
            if (ui.getSlot() != null) {
                ui.getSlot().setVisible(isActive);
                ui.getSlot().setManaged(isActive);
            }
            if (ui.getTaskHand() != null) {
                ui.getTaskHand().setVisible(isActive);
                ui.getTaskHand().setManaged(isActive);
            }
            if (ui.getCommunicationArea() != null) {
                ui.getCommunicationArea().setVisible(isActive);
                ui.getCommunicationArea().setManaged(isActive);
            }
            if (i < infoLabels.length && infoLabels[i] != null) {
                infoLabels[i].setVisible(isActive);
                infoLabels[i].setManaged(isActive);
            }
            
            // The hand pane is ONLY visible for the local player
            if (ui.getHand() != null) {
                boolean isMe = (i == myIndex);
                ui.getHand().setVisible(isActive && isMe);
                ui.getHand().setManaged(isActive && isMe);
            }
        }
    }

    public void updateInfoLabels() {
        if (session == null || session.getEngine() == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) {
            for (int i = 0; i < playerCount; i++) {
                if (i < infoLabels.length && infoLabels[i] != null) {
                    infoLabels[i].setText("Waiting for game start...");
                }
            }
            return;
        }

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        for (int i = 0; i < playerCount; i++) {
            if (i >= players.size()) {
                if (i < infoLabels.length && infoLabels[i] != null) {
                    infoLabels[i].setText("Waiting for player...");
                }
                continue;
            }
            int tricks = mission.getPlayerWinCount(i);
            int cards = players.get(i).getHand().size();
            String captain = i == mission.getCaptainIndex() ? "  [Captain]" : "";
            if (i < infoLabels.length && infoLabels[i] != null) {
                infoLabels[i].setText("Player " + (i + 1) + " | Tricks: " + tricks + "  Cards: " + cards + captain);
            }
        }
    }

    // =========================
    // RENDERING
    // =========================

    public void renderAllHands() {
        handUIManager.renderAllHands(session, playerCount, this::onCardClicked, this::onCommunicationCardSelected);
    }

    private void renderTasks() {
        taskUIManager.renderTasks(session, playerCount, this::onTaskClicked);
    }

    // =========================
    // EVENT HANDLERS
    // =========================

    private void onPassClicked() {
        int playerIndex = session.getEngine().getPlayerManager().getCurrentPlayerIndex();
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.PASS_TASK_SELECTION, null));
    }

    private void onTaskClicked(int playerIndex, Task task) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.SELECT_TASK, task));
    }

    private void onCardClicked(int playerIndex, Card card) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.PLAY_CARD, card));
    }

    private void onCommunicateClicked(int playerIndex) {
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.REQUEST_COMMUNICATION, null));
    }

    private void onCommunicationCardSelected(int playerIndex, Card card) {
        TokenPosition position = session.getEngine().getCommunicationManager().resolveCommunicationPosition(playerIndex, card);
        if (position == null) return;
        
        client.sendAction(new GameAction(playerIndex, GameAction.ActionType.SELECT_COMMUNICATION_CARD, new Object[]{card, position}));
    }

    @FXML
    private void onNextMissionClicked() {
        client.sendAction(new GameAction(GameApplication.playerInfo.getIndex(), GameAction.ActionType.NEXT_MISSION, null));
    }

    @FXML
    private void onRetryMissionClicked() {
        client.sendAction(new GameAction(GameApplication.playerInfo.getIndex(), GameAction.ActionType.RETRY_MISSION, null));
    }

    private void updateCommunicationUI() {
        if (session == null || session.getEngine() == null || playerUIs == null || commActionButtons == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        boolean phaseIsComm = session.getEngine().getPhase() == GamePhase.COMMUNICATION;
        boolean phaseIsTricking = session.getEngine().getPhase() == GamePhase.TRICKING;
        boolean showButtons = phaseIsComm || phaseIsTricking;
        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();

        for (int i = 0; i < playerCount; i++) {
            if (i >= playerUIs.size()) break;
            Pane commArea = playerUIs.get(i).getCommunicationArea();
            if (commArea == null) continue;
            commArea.getChildren().clear();

            if (i >= players.size()) continue;

            if (i >= commActionButtons.length) continue;
            Button communicationButton = commActionButtons[i];
            if (communicationButton == null) continue;
            commArea.getChildren().add(communicationButton);

            communicationButton.setManaged(showButtons);
            communicationButton.setVisible(showButtons);
            if (showButtons) {
                boolean isLocalPlayersButton = GameApplication.playerInfo != null && i == GameApplication.playerInfo.getIndex();

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
                    // TRICKING phase — any player with token available can communicate
                    boolean canCommunicate = !alreadyUsed &&
                        !session.getEngine().getCommunicationManager().getValidCommunicationCards(i, mission).isEmpty();
                    boolean alreadyRequested = session.getEngine().getCommunicationManager().isCommunicationRequested(i);
                    if (alreadyRequested) {
                        communicationButton.setStyle("-fx-background-color: orange; -fx-cursor: hand;");
                        communicationButton.setDisable(false);
                    } else if (canCommunicate) {
                        communicationButton.setStyle("-fx-background-color: green; -fx-cursor: hand;");
                        communicationButton.setDisable(false);
                    } else {
                        communicationButton.setStyle("-fx-background-color: grey; -fx-cursor: default;");
                        communicationButton.setDisable(true);
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

        // Auto-dismiss local player's active token after 5 seconds
        int localIndex = GameApplication.playerInfo != null ? GameApplication.playerInfo.getIndex() : -1;
        if (localIndex >= 0 && !dismissTimerScheduled) {
            for (CommunicationToken token : mission.getActiveTokens()) {
                if (token.getPlayerIndex() == localIndex) {
                    dismissTimerScheduled = true;
                    PauseTransition delay = new PauseTransition(Duration.seconds(5));
                    delay.setOnFinished(e -> {
                        client.sendAction(new GameAction(localIndex, GameAction.ActionType.DISMISS_COMMUNICATION, null));
                        dismissTimerScheduled = false;
                    });
                    delay.play();
                    break;
                }
            }
        }
    }

    private void clearTrickSlots() {
        trickUIManager.clearTrickSlots();
    }

    private void updateTaskUI() {
        taskUIManager.updateTaskUI(session, playerCount);
    }

    // =========================
    // UI UPDATES & HELPERS
    // =========================

    private void updateMissionLabels() {
        if (session == null || session.getEngine() == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) {
            missionLabel.setText("Mission: ?");
            difficultyLabel.setText("Difficulty: ?");
            missionDescriptionLabel.setText("Waiting for all players...");
            return;
        }
        missionLabel.setText("Mission " + session.getEngine().getCurrentMissionNumber());
        difficultyLabel.setText("Difficulty: " + (mission != null ? mission.getDifficulty() : "?"));
        missionDescriptionLabel.setText(mission != null ? mission.getDescription() : "");
    }

    private void updateCurrentPlayerLabel() {
        if (session == null || session.getEngine() == null || session.getEngine().getPlayerManager() == null) return;
        currentPlayerLabel.setText(
            "Current Turn: Player " + (session.getEngine().getPlayerManager().getCurrentPlayerIndex()+1)
        );
    }

    private void updatePhasePanels() {
        if (session == null || session.getEngine() == null) return;
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

        boolean isMyTurn = GameApplication.playerInfo != null &&
            session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.playerInfo.getIndex();
        passTaskSelectionButton.setDisable(!isMyTurn);

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
            missionResultOverlay.setVisible(false);
            missionResultOverlay.setManaged(false);
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
            if (client.isOutputStreamReady()) {
                client.sendAction(new GameAction(GameApplication.playerInfo.getIndex(), GameAction.ActionType.LOAD_GAME, state));
            } else {
                session.getEngine().restoreState(state);
                refreshAfterMissionTransition();
            }
        }
    }

    private void refreshAfterMissionTransition() {
        missionResultOverlay.setVisible(false);
        missionResultOverlay.setManaged(false);
        trickUIManager.clearTrickSlots();
        handUIManager.renderAllHands(session, playerCount, this::onCardClicked, this::onCommunicationCardSelected);
        trickUIManager.renderCurrentTrick(session);
        updateInfoLabels();
        taskUIManager.renderTasks(session, playerCount, this::onTaskClicked);
        taskUIManager.updateTaskUI(session, playerCount);
        updateMissionLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
        updateCommunicationUI();
    }

    public void renderCurrentTrick() {
        trickUIManager.renderCurrentTrick(session);
    }

    private int playerIndexFromTurn() {
        return session.getEngine().getPlayerManager().getCurrentPlayerIndex();
    }
}