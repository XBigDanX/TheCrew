package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.network.GameNetworkClient;
import game.thecrew.network.rmi.MissionClientCallback;
import game.thecrew.network.rmi.MissionService;
import game.thecrew.utils.FileUtils;
import game.thecrew.model.*;
import game.thecrew.ui.PlayerUI;
import game.thecrew.ui.managers.HandUIManager;
import game.thecrew.ui.managers.TaskUIManager;
import game.thecrew.ui.managers.TrickUIManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameController implements MissionClientCallback {
    @FXML private Label missionLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label missionDescriptionLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Button passTaskSelectionButton;
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
    @FXML private TextArea missionLogArea;
    @FXML public Label lobbyStatusLabel;

    private List<PlayerUI> playerUIs;
    private Label[] infoLabels;
    private GameSession session;
    private int playerCount;
    private GameNetworkClient client;
    private HandUIManager handUIManager;
    private TrickUIManager trickUIManager;
    private TaskUIManager taskUIManager;
    private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());

    public GameSession getSession() {
        return session;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    @FXML
    public void initialize() {
        try {
            if (GameApplication.getPlayerInfo() != null) {
                playerCount = GameApplication.getPlayerInfo().getTotalPlayers();
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

                handUIManager = new HandUIManager(playerUIs);
                trickUIManager = new TrickUIManager(slot0, slot1, slot2, slot3, slot4);
                taskUIManager = new TaskUIManager(
                    availableTasksBox,
                    taskHand0, taskHand1, taskHand2, taskHand3, taskHand4
                );

                setupPlayerViews();
                handUIManager.initCommButtons(
                    playerCount, this::onCommunicateClicked
                );
                refreshUI();
            }

            setupRMI();

        } catch (Exception exception) {
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
        int myPlayerIndex = (GameApplication.getPlayerInfo() != null)
            ? GameApplication.getPlayerInfo().getIndex()
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
            session, playerCount, this::onCardClicked, this::onCommunicationCardSelected
        );

        taskUIManager.renderTasks(
            session, this::onTaskClicked
        );
        taskUIManager.updateTaskUI(session);

        handUIManager.renderCommunicationUI(
            session, playerCount, this::onDismissCommunication
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
            this::onCardClicked,
            this::onCommunicationCardSelected
        );
    }

    public void updateInfoLabels() {
        if (session == null || session.getEngine() == null) {
            return;
        }

        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) {
            setAllLabels("Waiting for game start...");
            return;
        }

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        for (int i = 0; i < playerCount; i++) {
            setInfoLabel(i, players, mission);
        }
    }

    private void setAllLabels(String text) {
        for (int i = 0; i < Math.min(playerCount, infoLabels.length); i++) {
            if (infoLabels[i] != null) {
                infoLabels[i].setText(text);
            }
        }
    }

    private void setInfoLabel(int i, List<Player> players, Mission mission) {
        if (i >= infoLabels.length || infoLabels[i] == null) {
            return;
        }
        if (i >= players.size()) {
            infoLabels[i].setText("Waiting for player...");
            return;
        }
        Player player = players.get(i);
        String captainSuffix = (i == mission.getCaptainIndex()) ? "  [Captain]" : "";
        infoLabels[i].setText("Player " + (i + 1)
            + " | Tricks: " + mission.getPlayerWinCount(i)
            + "  Cards: " + player.getHand().size()
            + captainSuffix);
    }

    @FXML
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

    private void setupRMI() {
        try {
            UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry("localhost");
            MissionService service = (MissionService) registry.lookup("MissionService");
            String playerName = GameApplication.getPlayerInfo() != null
                ? "Player" + GameApplication.getPlayerInfo().getIndex()
                : "Unknown";
            service.registerClient(playerName, this);
            if (session != null && session.getEngine() != null) {
                session.getEngine().setMissionService(service);
            }
            LOGGER.log(Level.INFO, "[RMI] Registered with MissionService as {0}", playerName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[RMI] Failed to setup RMI", e);
        }
    }

    @Override
    public void updateLog(int missionId, boolean success, String playerName) throws RemoteException {
        String result = success ? "USPJEH" : "NEUSPJEH";
        String line = "Misija " + missionId + ": " + result;
        Platform.runLater(() -> {
            LOGGER.log(Level.INFO, "[RMI] {0}", line);
            if (missionLogArea != null) {
                missionLogArea.appendText(line + "\n");
            }
        });
    }

    @FXML
    private void onNextMissionClicked() {
        int myIndex = GameApplication.getPlayerInfo().getIndex();
        client.sendAction(
            new GameAction(myIndex, GameAction.ActionType.NEXT_MISSION, null)
        );
    }

    @FXML
    private void onRetryMissionClicked() {
        int myIndex = GameApplication.getPlayerInfo().getIndex();
        client.sendAction(
            new GameAction(myIndex, GameAction.ActionType.RETRY_MISSION, null)
        );
    }

    @FXML
    private void onSaveClicked() {
        FileUtils.save(session.getEngine().saveState());
    }

    @FXML
    private void onLoadClicked() {
        GameState gameState = FileUtils.load();
        if (gameState == null) {
            return;
        }

        if (client.isOutputStreamReady()) {
            int myIndex = GameApplication.getPlayerInfo().getIndex();
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

    public void showTrickWinner(int winnerId) {
        currentPlayerLabel.setText("Player " + (winnerId + 1) + " won the trick!");
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
            boolean myTurn = GameApplication.getPlayerInfo() != null
                && session.getEngine().getPlayerManager().getCurrentPlayerIndex()
                    == GameApplication.getPlayerInfo().getIndex();
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
