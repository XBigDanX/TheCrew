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
        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
        Card clickedCard = card;

        if (currentPhase == GamePhase.COMMUNICATION && communicatingPlayerIndex == playerIndex) {
            setupCommSelection(cardView, validCommCards, actionSender, playerIndex, clickedCard, session);
        } else if (isMyTurn(playerIndex, session) && currentPhase == GamePhase.TRICKING) {
            setupTrickSelection(cardView, validCommCards, actionSender, playerIndex, clickedCard, communicatingPlayerIndex, session);
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

    private void setupTrickSelection(CardView cardView, List<Card> validCommCards, NetworkActionSender actionSender,
                                      int playerIndex, Card clickedCard, int communicatingPlayerIndex, GameSession session) {
        if (validCommCards != null && validCommCards.contains(clickedCard)) {
            setupCommSelection(cardView, validCommCards, actionSender, playerIndex, clickedCard, session);
        } else if (communicatingPlayerIndex == -1) {
            cardView.setOnMouseClicked(e -> actionSender.playCard(playerIndex, clickedCard));
        }
    }

    public void renderCommunicationUI(GameSession session, int playerCount, NetworkActionSender actionSender, TrickUIManager trickUIManager) {
        if (session == null || session.getEngine() == null || commActionButtons == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        CommunicationToken[] pending = session.getEngine().getPendingTokens();

        for (int i = 0; i < playerCount; i++) {
            if (i < commAreas.length && commAreas[i] != null && i < players.size() && i < commActionButtons.length && commActionButtons[i] != null) {
                Pane commArea = commAreas[i];
                commArea.getChildren().clear();

                Button commButton = commActionButtons[i];
                commArea.getChildren().add(commButton);

                updateCommButtonStyle(commButton, i, session, trickUIManager);

                renderTokensForPlayer(commArea, i, mission, pending);
            }
        }

        int localIndex = GameApplication.getPlayerInfo() != null ? GameApplication.getPlayerInfo().getIndex() : -1;
        scheduleDismissTimer(mission, localIndex, actionSender);
    }

    public void updateCommButtonStyle(Button btn, int playerIndex, GameSession session, TrickUIManager trickUIManager) {
        if (session == null || session.getEngine() == null) return;

        Mission mission = session.getEngine().getCurrentMission();
        if (mission != null && mission.hasPlayerUsedToken(playerIndex)) {
            btn.setStyle(RED_USED_STYLE);
            btn.setDisable(true);
            btn.setVisible(true);
            btn.setManaged(true);
            return;
        }

        boolean phaseIsComm = session.getEngine().getPhase() == GamePhase.COMMUNICATION;
        boolean isTricking = session.getEngine().getPhase() == GamePhase.TRICKING;
        boolean visible = phaseIsComm || isTricking;

        btn.setVisible(visible);
        btn.setManaged(visible);

        if (!visible) return;

        if (!trickUIManager.isTrickPaneEmpty(session)) {
            btn.setDisable(true);
            return;
        }

        int localPlayerIndex = (GameApplication.getPlayerInfo() != null) ? GameApplication.getPlayerInfo().getIndex() : -1;
        
        if (playerIndex != localPlayerIndex) {
            btn.setDisable(true);
            return;
        }

        boolean canComm = !session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, mission).isEmpty();

        if (phaseIsComm) {
            int commIdx = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
            if (commIdx == playerIndex) {
                btn.setStyle("-fx-background-color: red; -fx-cursor: hand;");
                btn.setDisable(false);
            } else if (commIdx == -1) {
                btn.setStyle(canComm ? GREEN_HAND_STYLE : "-fx-background-color: grey; -fx-cursor: default;");
                btn.setDisable(!canComm);
            } else {
                btn.setDisable(true);
            }
        } else {
            boolean requested = session.getEngine().getCommunicationManager().isCommunicationRequested(playerIndex);
            if (requested) {
                btn.setStyle("-fx-background-color: orange; -fx-cursor: hand;");
                btn.setDisable(false);
            } else if (canComm) {
                btn.setStyle(GREEN_HAND_STYLE);
                btn.setDisable(false);
            } else {
                btn.setStyle("-fx-background-color: grey; -fx-cursor: default;");
                btn.setDisable(true);
            }
        }
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
