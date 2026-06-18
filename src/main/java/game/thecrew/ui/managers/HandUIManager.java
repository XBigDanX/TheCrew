package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.Card;
import game.thecrew.model.CommunicationToken;
import game.thecrew.model.GamePhase;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;
import game.thecrew.model.TokenPosition;
import game.thecrew.network.NetworkActionSender;
import game.thecrew.ui.CardView;
import game.thecrew.ui.PlayerUI;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;

public class HandUIManager {

    private final FlowPane[] hands;
    private final StackPane[] commAreas;
    private Button[] commActionButtons;
    private boolean dismissTimerScheduled;

    private static final String GREEN_HAND_STYLE = "-fx-background-color: green; -fx-cursor: hand;";
    private static final String RED_USED_STYLE = "-fx-background-color: red; -fx-cursor: default;";

    public HandUIManager(List<PlayerUI> playerUIs) {
        int size = playerUIs.size();
        this.hands = new FlowPane[size];
        this.commAreas = new StackPane[size];

        for (int i = 0; i < size; i++) {
            PlayerUI ui = playerUIs.get(i);
            this.hands[i] = ui.getHand();
            this.commAreas[i] = (StackPane) ui.getCommunicationArea();
        }
    }

    public void initCommButtons(int playerCount, NetworkActionSender actionSender) {
        commActionButtons = new Button[playerCount];
        for (int i = 0; i < playerCount; i++) {
            Button button = new Button();
            button.setMinSize(30, 30);
            button.setMaxSize(30, 30);
            button.setShape(new Circle(15));
            button.setStyle(GREEN_HAND_STYLE);
            button.setManaged(false);
            button.setVisible(false);
            int index = i;
            button.setOnAction(e -> actionSender.requestCommunication(index));
            StackPane.setAlignment(button, Pos.CENTER);
            if (commAreas[i] != null) {
                commAreas[i].getChildren().add(button);
            }
            commActionButtons[i] = button;
        }
    }

    public void renderAllHands(GameSession session, int playerCount, NetworkActionSender actionSender) {
        for (int i = 0; i < playerCount; i++) {
            renderPlayerHand(session, i, actionSender);
        }
    }

    private void renderPlayerHand(GameSession session, int playerIndex, NetworkActionSender actionSender) {
        if (playerIndex >= hands.length || hands[playerIndex] == null) return;
        if (session == null || session.getEngine() == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        if (playerIndex >= players.size()) return;

        Player player = players.get(playerIndex);
        FlowPane handPane = hands[playerIndex];
        handPane.getChildren().clear();

        boolean isLocalPlayer = GameApplication.getPlayerInfo() != null && playerIndex == GameApplication.getPlayerInfo().getIndex();

        if (isLocalPlayer) {
            renderLocalHand(session, playerIndex, handPane, player, actionSender);
        } else {
            renderRemoteHand(handPane, player);
        }
    }

    private void renderLocalHand(GameSession session, int playerIndex, FlowPane handPane, Player player,
                                  NetworkActionSender actionSender) {
        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
        List<Card> validCommCards = (communicatingPlayerIndex == playerIndex)
                ? session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, session.getEngine().getCurrentMission())
                : null;

        for (Card card : player.getHand()) {
            CardView cardView = new CardView(card);
            setupCardInteraction(cardView, card, playerIndex, session, validCommCards, actionSender);
            handPane.getChildren().add(cardView);
        }
    }

    private void renderRemoteHand(FlowPane handPane, Player player) {
        for (int i = 0; i < player.getHand().size(); i++) {
            handPane.getChildren().add(CardView.createBack());
        }
    }

    private void setupCardInteraction(CardView cardView, Card card, int playerIndex, GameSession session,
                                       List<Card> validCommCards, NetworkActionSender actionSender) {
        GamePhase currentPhase = session.getEngine().getPhase();
        int commIdx = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();

        if (currentPhase == GamePhase.COMMUNICATION && commIdx == playerIndex) {
            setupCommSelection(cardView, validCommCards, actionSender, playerIndex, card, session);
        } else if (currentPhase == GamePhase.TRICKING && isMyTurn(playerIndex, session)) {
            setupTrickingInteraction(cardView, validCommCards, actionSender, playerIndex, card, commIdx, session);
        }
    }

    private void setupTrickingInteraction(CardView cardView, List<Card> validCommCards, NetworkActionSender actionSender,
                                           int playerIndex, Card card, int commIdx, GameSession session) {
        if (validCommCards != null && validCommCards.contains(card)) {
            setupCommSelection(cardView, validCommCards, actionSender, playerIndex, card, session);
        } else if (commIdx == -1) {
            cardView.setOnMouseClicked(e -> actionSender.playCard(playerIndex, card));
        }
    }

    private boolean isMyTurn(int playerIndex, GameSession session) {
        return GameApplication.getPlayerInfo() != null
                && playerIndex == GameApplication.getPlayerInfo().getIndex()
                && session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.getPlayerInfo().getIndex();
    }

    private void setupCommSelection(CardView cardView, List<Card> validCommCards, NetworkActionSender actionSender,
                                     int playerIndex, Card clickedCard, GameSession session) {
        if (validCommCards != null && validCommCards.contains(clickedCard)) {
            cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
            cardView.setOnMouseClicked(e -> {
                TokenPosition tp = session.getEngine().getCommunicationManager()
                    .resolveCommunicationPosition(playerIndex, clickedCard);
                if (tp != null) actionSender.selectCommunicationCard(playerIndex, clickedCard, tp);
            });
        }
    }


    public void renderCommunicationUI(GameSession session, int playerCount, NetworkActionSender actionSender, TrickUIManager trickUIManager) {
        if (session == null || session.getEngine() == null || commActionButtons == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        CommunicationToken[] pending = session.getEngine().getPendingTokens();

        for (int i = 0; i < playerCount; i++) {
            renderPlayerCommunication(i, session, mission, players, pending, trickUIManager);
        }

        int localIndex = GameApplication.getPlayerInfo() != null ? GameApplication.getPlayerInfo().getIndex() : -1;
        scheduleDismissTimer(mission, localIndex, actionSender);
    }

    private void renderPlayerCommunication(int playerIndex, GameSession session, Mission mission,
                                           List<Player> players, CommunicationToken[] pending,
                                           TrickUIManager trickUIManager) {
        if (playerIndex >= commAreas.length || commAreas[playerIndex] == null ||
            playerIndex >= players.size() || playerIndex >= commActionButtons.length ||
            commActionButtons[playerIndex] == null) {
            return;
        }

        Pane commArea = commAreas[playerIndex];
        commArea.getChildren().clear();

        Button commButton = commActionButtons[playerIndex];
        commArea.getChildren().add(commButton);

        updateCommButtonStyle(commButton, playerIndex, session, trickUIManager);
        renderTokensForPlayer(commArea, playerIndex, mission, pending);
    }

    public void updateCommButtonStyle(Button btn, int playerIndex, GameSession session, TrickUIManager trickUIManager) {
        if (session == null || session.getEngine() == null) return;

        Mission mission = session.getEngine().getCurrentMission();
        if (mission != null && mission.hasPlayerUsedToken(playerIndex)) {
            setButtonStyle(btn, RED_USED_STYLE, true, true);
            return;
        }

        GamePhase phase = session.getEngine().getPhase();
        boolean visible = phase == GamePhase.COMMUNICATION || phase == GamePhase.TRICKING;
        btn.setVisible(visible);
        btn.setManaged(visible);

        if (!visible || !isButtonActiveForLocalPlayer(playerIndex, session, trickUIManager)) {
            btn.setDisable(true);
            return;
        }

        boolean canComm = !session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, mission).isEmpty();
        if (phase == GamePhase.COMMUNICATION) {
            updateCommPhaseStyle(btn, playerIndex, session, canComm);
        } else {
            updateTrickingPhaseStyle(btn, playerIndex, session, canComm);
        }
    }

    private boolean isButtonActiveForLocalPlayer(int playerIndex, GameSession session, TrickUIManager trickUIManager) {
        if (!trickUIManager.isTrickPaneEmpty(session)) return false;
        int localPlayerIndex = (GameApplication.getPlayerInfo() != null) ? GameApplication.getPlayerInfo().getIndex() : -1;
        return playerIndex == localPlayerIndex;
    }

    private void updateCommPhaseStyle(Button btn, int playerIndex, GameSession session, boolean canComm) {
        int commIdx = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
        if (commIdx == playerIndex) {
            setButtonStyle(btn, "-fx-background-color: red; -fx-cursor: hand;", false, true);
        } else if (commIdx == -1) {
            setButtonStyle(btn, canComm ? GREEN_HAND_STYLE : "-fx-background-color: grey; -fx-cursor: default;", !canComm, true);
        } else {
            btn.setDisable(true);
        }
    }

    private void updateTrickingPhaseStyle(Button btn, int playerIndex, GameSession session, boolean canComm) {
        boolean requested = session.getEngine().getCommunicationManager().isCommunicationRequested(playerIndex);
        if (requested) {
            setButtonStyle(btn, "-fx-background-color: orange; -fx-cursor: hand;", false, true);
        } else if (canComm) {
            setButtonStyle(btn, GREEN_HAND_STYLE, false, true);
        } else {
            setButtonStyle(btn, "-fx-background-color: grey; -fx-cursor: default;", true, true);
        }
    }

    private void setButtonStyle(Button btn, String style, boolean disabled, boolean visible) {
        btn.setStyle(style);
        btn.setDisable(disabled);
        btn.setVisible(visible);
        btn.setManaged(visible);
    }

    private void renderTokensForPlayer(Pane commArea, int playerIndex, Mission mission, CommunicationToken[] pending) {
        for (CommunicationToken token : mission.getActiveTokens()) {
            if (token.getPlayerIndex() == playerIndex) {
                CardView cv = new CardView(token.getCard());
                cv.addToken(token.getPosition());
                commArea.getChildren().add(cv);
            }
        }

        if (pending != null && pending[playerIndex] != null) {
            CardView cv = new CardView(pending[playerIndex].getCard());
            cv.addToken(pending[playerIndex].getPosition());
            cv.setOpacity(0.6);
            commArea.getChildren().add(cv);
        }
    }

    public void setupVisibility(int playerCount) {
        int myIdx = GameApplication.getPlayerInfo() != null ? GameApplication.getPlayerInfo().getIndex() : -1;
        for (int i = 0; i < hands.length; i++) {
            boolean active = i < playerCount;
            boolean isMine = i == myIdx;
            if (hands[i] != null) {
                hands[i].setVisible(active && isMine);
                hands[i].setManaged(active && isMine);
            }
            if (commAreas[i] != null) {
                commAreas[i].setVisible(active);
                commAreas[i].setManaged(active);
            }
        }
    }

    private void scheduleDismissTimer(Mission mission, int localIndex, NetworkActionSender actionSender) {
        if (localIndex < 0 || dismissTimerScheduled) return;

        for (CommunicationToken token : mission.getActiveTokens()) {
            if (token.getPlayerIndex() == localIndex) {
                dismissTimerScheduled = true;
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(e -> {
                    actionSender.dismissCommunication(localIndex);
                    dismissTimerScheduled = false;
                });
                delay.play();
                break;
            }
        }
    }
}
