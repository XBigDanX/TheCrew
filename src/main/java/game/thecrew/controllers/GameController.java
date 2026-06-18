package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.network.GameNetworkClient;
import game.thecrew.network.NetworkActionSender;
import game.thecrew.network.rmi.MissionClientCallback;
import game.thecrew.utils.FileUtils;
import game.thecrew.model.GameState;
import game.thecrew.ui.PlayerUI;
import game.thecrew.ui.managers.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.rmi.RemoteException;
import java.util.List;

public class GameController implements MissionClientCallback {
    @FXML private Label missionLabel, difficultyLabel, missionDescriptionLabel, currentPlayerLabel;
    @FXML private Label infoLabel0, infoLabel1, infoLabel2, infoLabel3, infoLabel4;
    @FXML private Label resultTitleLabel, resultMessageLabel;
    @FXML public Label lobbyStatusLabel;
    @FXML private Button passTaskSelectionButton, nextMissionButton, retryButton;
    @FXML private Pane taskPane, trickPane, slot0, slot1, slot2, slot3, slot4;
    @FXML private HBox availableTasksBox, taskHand0, taskHand1, taskHand2, taskHand3, taskHand4;
    @FXML private FlowPane hand0, hand1, hand2, hand3, hand4;
    @FXML private StackPane commArea0, commArea1, commArea2, commArea3, commArea4;
    @FXML private StackPane missionResultOverlay;
    @FXML public StackPane lobbyOverlay;
    @FXML private TextArea missionLogArea;

    private List<PlayerUI> playerUIs;
    private Label[] infoLabels;
    private GameSession session;
    private int playerCount;
    private GameNetworkClient client;
    private NetworkActionSender actionSender;
    private HandUIManager handUIManager;
    private TrickUIManager trickUIManager;
    private TaskUIManager taskUIManager;
    private MissionInfoManager missionInfoManager;
    private PlayerInfoManager playerInfoManager;
    private MissionResultManager missionResultManager;
    private RMIManager rmiManager;

    public GameSession getSession() { return session; }
    public void setSession(GameSession session) { this.session = session; }
    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
    public TrickUIManager getTrickUIManager() { return trickUIManager; }
    public List<PlayerUI> getPlayerUIs() { return playerUIs; }

    @FXML
    public void initialize() {
        try {
            if (GameApplication.getPlayerInfo() != null) {
                playerCount = GameApplication.getPlayerInfo().getTotalPlayers();
                if (playerCount > 0) session = new GameSession(playerCount);
            }

            client = new GameNetworkClient();
            actionSender = new NetworkActionSender(client);
            if (client.isConnected()) client.listen(this);

            if (session != null) {
                initPlayerUIs();

                handUIManager = new HandUIManager(playerUIs);
                trickUIManager = new TrickUIManager(slot0, slot1, slot2, slot3, slot4);
                taskUIManager = new TaskUIManager(availableTasksBox,
                    taskHand0, taskHand1, taskHand2, taskHand3, taskHand4);
                missionInfoManager = new MissionInfoManager(missionLabel, difficultyLabel,
                    missionDescriptionLabel, currentPlayerLabel, taskPane, trickPane, passTaskSelectionButton);
                playerInfoManager = new PlayerInfoManager(infoLabels);
                missionResultManager = new MissionResultManager(missionResultOverlay,
                    resultTitleLabel, resultMessageLabel, nextMissionButton, retryButton);
                rmiManager = new RMIManager(missionLogArea);
                rmiManager.setSession(session);

                setupPlayerViews();
                handUIManager.initCommButtons(playerCount, actionSender);
                refreshUI();
            }

            rmiManager.setupRMI(this);

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
        infoLabels = new Label[]{infoLabel0, infoLabel1, infoLabel2, infoLabel3, infoLabel4};
    }

    public void setupPlayerViews() {
        int myIdx = GameApplication.getPlayerInfo() != null
            ? GameApplication.getPlayerInfo().getIndex() : -1;

        for (int i = 0; i < playerUIs.size(); i++) {
            boolean active = i < playerCount;
            PlayerUI pui = playerUIs.get(i);

            if (pui.getSlot() != null) {
                pui.getSlot().setVisible(active);
                pui.getSlot().setManaged(active);
            }
            if (pui.getTaskHand() != null) {
                pui.getTaskHand().setVisible(active);
                pui.getTaskHand().setManaged(active);
            }
            if (pui.getCommunicationArea() != null) {
                pui.getCommunicationArea().setVisible(active);
                pui.getCommunicationArea().setManaged(active);
            }
            if (i < infoLabels.length && infoLabels[i] != null) {
                infoLabels[i].setVisible(active);
                infoLabels[i].setManaged(active);
            }
            if (pui.getHand() != null) {
                boolean mine = i == myIdx;
                pui.getHand().setVisible(active && mine);
                pui.getHand().setManaged(active && mine);
            }
        }
    }

    public void refreshUI() {
        trickUIManager.clearTrickSlots();
        trickUIManager.renderCurrentTrick(session);
        handUIManager.renderAllHands(session, playerCount, actionSender);
        taskUIManager.renderTasks(session, actionSender);
        taskUIManager.updateTaskUI(session);
        handUIManager.renderCommunicationUI(session, playerCount, actionSender);
        playerInfoManager.updateInfoLabels(session, playerCount);
        missionInfoManager.updateMissionLabels(session);
        missionInfoManager.updateCurrentPlayerLabel(session);
        missionInfoManager.updatePhasePanels(session);
        missionResultManager.handleMissionEnd(session);
    }

    public void renderCurrentTrick() { trickUIManager.renderCurrentTrick(session); }
    public void renderAllHands() { handUIManager.renderAllHands(session, playerCount, actionSender); }
    public void updateInfoLabels() { playerInfoManager.updateInfoLabels(session, playerCount); }
    public void showTrickWinner(int winnerId) { missionInfoManager.showTrickWinner(winnerId); }

    @FXML
    private void onPassClicked() {
        actionSender.passTaskSelection(session.getEngine().getPlayerManager().getCurrentPlayerIndex());
    }

    @FXML
    private void onNextMissionClicked() {
        actionSender.nextMission(GameApplication.getPlayerInfo().getIndex());
    }

    @FXML
    private void onRetryMissionClicked() {
        actionSender.retryMission(GameApplication.getPlayerInfo().getIndex());
    }

    @FXML
    private void onSaveClicked() {
        FileUtils.save(session.getEngine().saveState());
    }

    @FXML
    private void onLoadClicked() {
        GameState gameState = FileUtils.load();
        if (gameState == null) return;

        if (client.isOutputStreamReady()) {
            actionSender.loadGame(GameApplication.getPlayerInfo().getIndex(), gameState);
        } else {
            session.getEngine().restoreState(gameState);
            missionResultOverlay.setVisible(false);
            missionResultOverlay.setManaged(false);
            refreshUI();
        }
    }

    @Override
    public void updateLog(int missionId, boolean success, String playerName) throws RemoteException {
        rmiManager.updateLog(missionId, success, playerName);
    }
}
