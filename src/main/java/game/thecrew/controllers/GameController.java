package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.network.GameNetworkClient;
import game.thecrew.utils.FileUtils;
import game.thecrew.model.*;
import game.thecrew.ui.PlayerUI;
import game.thecrew.ui.managers.HandUIManager;
import game.thecrew.ui.managers.TaskUIManager;
import game.thecrew.ui.managers.TrickUIManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class GameController {
    @FXML private Label missionLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label missionDescriptionLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Button passTaskSelectionButton;
    @FXML private Button communicateButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Pane taskPane;
    @FXML private Pane trickPane;
    @FXML private HBox availableTasksBox;
    @FXML private Pane slot0;
    @FXML private Pane slot1;
    @FXML private Pane slot2;
    @FXML private Pane slot3;
    @FXML private Pane slot4;
    @FXML private FlowPane hand0;
    @FXML private FlowPane hand1;
    @FXML private FlowPane hand2;
    @FXML private FlowPane hand3;
    @FXML private FlowPane hand4;
    @FXML private HBox taskHand0;
    @FXML private HBox taskHand1;
    @FXML private HBox taskHand2;
    @FXML private HBox taskHand3;
    @FXML private HBox taskHand4;
    @FXML private StackPane commArea0;
    @FXML private StackPane commArea1;
    @FXML private StackPane commArea2;
    @FXML private StackPane commArea3;
    @FXML private StackPane commArea4;
    @FXML private Label infoLabel0;
    @FXML private Label infoLabel1;
    @FXML private Label infoLabel2;
    @FXML private Label infoLabel3;
    @FXML private Label infoLabel4;
    @FXML private StackPane missionResultOverlay;
    @FXML private Label resultTitleLabel;
    @FXML private Label resultMessageLabel;
    @FXML private Button nextMissionButton;
    @FXML private Button retryButton;
    @FXML public StackPane lobbyOverlay;
    @FXML public Label lobbyStatusLabel;

    private List<PlayerUI> playerUIs;
    private Label[] infoLabels;
    public GameSession session;
    public int playerCount;
    private GameNetworkClient client;
    private HandUIManager handUIManager;
    private TrickUIManager trickUIManager;
    private TaskUIManager taskUIManager;

    @FXML
    public void initialize() {
        try {
            if (GameApplication.playerInfo != null) {
                playerCount = GameApplication.playerInfo.getTotalPlayers();
                if (playerCount > 0) {
                    session = new GameSession(playerCount);
                }
            }

            client = new GameNetworkClient();
            if (client.isConnected()) {
                client.listen(this);
            }

            if (session != null) {
                initPlayerUIs();

                handUIManager = new HandUIManager(
                    hand0, hand1, hand2, hand3, hand4,
                    commArea0, commArea1, commArea2, commArea3, commArea4
                );
                trickUIManager = new TrickUIManager(slot0, slot1, slot2, slot3, slot4);
                taskUIManager = new TaskUIManager(
                    availableTasksBox,
                    taskHand0, taskHand1, taskHand2, taskHand3, taskHand4
                );

                setupPlayerViews();
                handUIManager.initCommButtons(
                    playerCount,
                    playerIndex -> onCommunicateClicked(playerIndex)
                );
                refreshUI();
            }

            passTaskSelectionButton.setOnAction(event -> onPassClicked());
            nextMissionButton.setOnAction(event -> onNextMissionClicked());
            retryButton.setOnAction(event -> onRetryMissionClicked());
            saveButton.setOnAction(event -> onSaveClicked());
            loadButton.setOnAction(event -> onLoadClicked());
        } catch (Exception exception) {
            System.err.println("[DEBUG_LOG] Error in GameController.initialize():");
            exception.printStackTrace();
            throw exception;
        }
    }

    public void initPlayerUIs() {
        playerUIs = List.of(
            new PlayerUI(hand0, slot0, taskHand0, commArea0),
            new PlayerUI(hand1, slot1, taskHand1, commArea1),
            new PlayerUI(hand2, slot2, taskHand2, commArea2),
            new PlayerUI(hand3, slot3, taskHand3, commArea3),
            new PlayerUI(hand4, slot4, taskHand4, commArea4)
        );
        infoLabels = new Label[]{
            infoLabel0, infoLabel1, infoLabel2, infoLabel3, infoLabel4
        };
    }

    public void setupPlayerViews() {
        int myPlayerIndex = (GameApplication.playerInfo != null)
            ? GameApplication.playerInfo.getIndex()
            : -1;

        for (int i = 0; i < playerUIs.size(); i++) {
            boolean active = i < playerCount;
            PlayerUI playerUI = playerUIs.get(i);

            if (playerUI.getSlot() != null) {
                playerUI.getSlot().setVisible(active);
                playerUI.getSlot().setManaged(active);
            }
            if (playerUI.getTaskHand() != null) {
                playerUI.getTaskHand().setVisible(active);
                playerUI.getTaskHand().setManaged(active);
            }
            if (playerUI.getCommunicationArea() != null) {
                playerUI.getCommunicationArea().setVisible(active);
                playerUI.getCommunicationArea().setManaged(active);
            }
            if (i < infoLabels.length && infoLabels[i] != null) {
                infoLabels[i].setVisible(active);
                infoLabels[i].setManaged(active);
            }
            if (playerUI.getHand() != null) {
                boolean isMine = i == myPlayerIndex;
                playerUI.getHand().setVisible(active && isMine);
                playerUI.getHand().setManaged(active && isMine);
            }
        }
    }

    public void refreshUI() {
        trickUIManager.clearTrickSlots();
        trickUIManager.renderCurrentTrick(session);

        handUIManager.renderAllHands(
            session, playerCount,
            (playerIndex, card) -> onCardClicked(playerIndex, card),
            (playerIndex, card) -> onCommunicationCardSelected(playerIndex, card)
        );

        taskUIManager.renderTasks(
            session, playerCount,
            (playerIndex, task) -> onTaskClicked(playerIndex, task)
        );
        taskUIManager.updateTaskUI(session, playerCount);

        handUIManager.renderCommunicationUI(
            session, playerCount,
            playerIndex -> onDismissCommunication(playerIndex)
        );

        updateInfoLabels();
        updateMissionLabels();
        updateCurrentPlayerLabel();
        updatePhasePanels();
        handleMissionEnd();
    }

    public void renderCurrentTrick() {
        trickUIManager.renderCurrentTrick(session);
    }

    public void renderAllHands() {
        handUIManager.renderAllHands(
            session, playerCount,
            (playerIndex, card) -> onCardClicked(playerIndex, card),
            (playerIndex, card) -> onCommunicationCardSelected(playerIndex, card)
        );
    }

    public void updateInfoLabels() {
        if (session == null || session.getEngine() == null) {
            return;
        }

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

            String captainSuffix = (i == mission.getCaptainIndex()) ? "  [Captain]" : "";
            if (i < infoLabels.length && infoLabels[i] != null) {
                Player player = players.get(i);
                String labelText = "Player " + (i + 1)
                    + " | Tricks: " + mission.getPlayerWinCount(i)
                    + "  Cards: " + player.getHand().size()
                    + captainSuffix;
                infoLabels[i].setText(labelText);
            }
        }
    }

    private void onPassClicked() {
        int currentPlayerIndex = session.getEngine().getPlayerManager().getCurrentPlayerIndex();
        client.sendAction(
            new GameAction(currentPlayerIndex, GameAction.ActionType.PASS_TASK_SELECTION, null)
        );
    }

    private void onTaskClicked(int playerIndex, Task task) {
        client.sendAction(
            new GameAction(playerIndex, GameAction.ActionType.SELECT_TASK, task)
        );
    }

    private void onCardClicked(int playerIndex, Card card) {
        client.sendAction(
            new GameAction(playerIndex, GameAction.ActionType.PLAY_CARD, card)
        );
    }

    private void onCommunicateClicked(int playerIndex) {
        client.sendAction(
            new GameAction(playerIndex, GameAction.ActionType.REQUEST_COMMUNICATION, null)
        );
    }

    private void onCommunicationCardSelected(int playerIndex, Card card) {
        TokenPosition tokenPosition = session.getEngine()
            .getCommunicationManager()
            .resolveCommunicationPosition(playerIndex, card);
        if (tokenPosition != null) {
            client.sendAction(
                new GameAction(
                    playerIndex,
                    GameAction.ActionType.SELECT_COMMUNICATION_CARD,
                    new Object[]{card, tokenPosition}
                )
            );
        }
    }

    private void onDismissCommunication(int playerIndex) {
        client.sendAction(
            new GameAction(playerIndex, GameAction.ActionType.DISMISS_COMMUNICATION, null)
        );
    }

    private void onNextMissionClicked() {
        int myIndex = GameApplication.playerInfo.getIndex();
        client.sendAction(
            new GameAction(myIndex, GameAction.ActionType.NEXT_MISSION, null)
        );
    }

    private void onRetryMissionClicked() {
        int myIndex = GameApplication.playerInfo.getIndex();
        client.sendAction(
            new GameAction(myIndex, GameAction.ActionType.RETRY_MISSION, null)
        );
    }

    private void onSaveClicked() {
        FileUtils.save(session.getEngine().saveState());
    }

    private void onLoadClicked() {
        GameState gameState = FileUtils.load();
        if (gameState == null) {
            return;
        }

        if (client.isOutputStreamReady()) {
            int myIndex = GameApplication.playerInfo.getIndex();
            client.sendAction(
                new GameAction(myIndex, GameAction.ActionType.LOAD_GAME, gameState)
            );
        } else {
            session.getEngine().restoreState(gameState);
            missionResultOverlay.setVisible(false);
            missionResultOverlay.setManaged(false);
            refreshUI();
        }
    }

    private void updateMissionLabels() {
        if (session == null || session.getEngine() == null) {
            return;
        }

        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) {
            missionLabel.setText("Mission: ?");
            difficultyLabel.setText("Difficulty: ?");
            missionDescriptionLabel.setText("Waiting for all players...");
            return;
        }

        missionLabel.setText("Mission " + session.getEngine().getCurrentMissionNumber());
        difficultyLabel.setText("Difficulty: " + mission.getDifficulty());
        missionDescriptionLabel.setText(mission.getDescription());
    }

    private void updateCurrentPlayerLabel() {
        if (session == null
            || session.getEngine() == null
            || session.getEngine().getPlayerManager() == null) {
            return;
        }

        int currentPlayerIndex = session.getEngine().getPlayerManager().getCurrentPlayerIndex();
        currentPlayerLabel.setText("Current Turn: Player " + (currentPlayerIndex + 1));
    }

    private void updatePhasePanels() {
        boolean taskPhase = session != null
            && session.getEngine() != null
            && session.getEngine().getPhase() == GamePhase.TASK_SELECTION;

        taskPane.setVisible(taskPhase);
        taskPane.setManaged(taskPhase);
        passTaskSelectionButton.setVisible(taskPhase);
        passTaskSelectionButton.setManaged(taskPhase);

        if (taskPhase) {
            boolean myTurn = GameApplication.playerInfo != null
                && session.getEngine().getPlayerManager().getCurrentPlayerIndex()
                    == GameApplication.playerInfo.getIndex();
            passTaskSelectionButton.setDisable(!myTurn);
        }

        trickPane.setVisible(!taskPhase);
        trickPane.setManaged(!taskPhase);
    }

    private void handleMissionEnd() {
        boolean missionComplete =
            session.getEngine().getPhase() == GamePhase.MISSION_COMPLETE;

        missionResultOverlay.setVisible(missionComplete);
        missionResultOverlay.setManaged(missionComplete);

        if (!missionComplete) {
            return;
        }

        boolean missionSuccess =
            session.getEngine().getCurrentMission().getStatus() == MissionStatus.SUCCESS;

        resultTitleLabel.setText(
            missionSuccess ? "Mission Complete!" : "Mission Failed"
        );
        resultMessageLabel.setText(
            missionSuccess
                ? "All tasks were completed."
                : "Not all tasks were completed."
        );

        nextMissionButton.setVisible(missionSuccess);
        nextMissionButton.setManaged(missionSuccess);
        retryButton.setVisible(!missionSuccess);
        retryButton.setManaged(!missionSuccess);
    }
}
