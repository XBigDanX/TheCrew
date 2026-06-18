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
import javafx.animation.SequentialTransition;
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
        boolean isMyTurn = GameApplication.getPlayerInfo() != null
                && playerIndex == GameApplication.getPlayerInfo().getIndex()
                && session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.getPlayerInfo().getIndex();

        Card clickedCard = card;

        if (currentPhase == GamePhase.COMMUNICATION && communicatingPlayerIndex == playerIndex) {
            if (validCommCards != null && validCommCards.contains(card)) {
                cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                cardView.setOnMouseClicked(e -> {
                    TokenPosition tp = session.getEngine().getCommunicationManager()
                        .resolveCommunicationPosition(playerIndex, clickedCard);
                    if (tp != null) actionSender.selectCommunicationCard(playerIndex, clickedCard, tp);
                });
            }
        } else if (isMyTurn && currentPhase == GamePhase.TRICKING) {
            if (validCommCards != null && validCommCards.contains(card)) {
                cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                cardView.setOnMouseClicked(e -> {
                    TokenPosition tp = session.getEngine().getCommunicationManager()
                        .resolveCommunicationPosition(playerIndex, clickedCard);
                    if (tp != null) actionSender.selectCommunicationCard(playerIndex, clickedCard, tp);
                });
            } else if (communicatingPlayerIndex == -1) {
                cardView.setOnMouseClicked(e -> actionSender.playCard(playerIndex, clickedCard));
            }
        }
    }

    public void renderCommunicationUI(GameSession session, int playerCount, NetworkActionSender actionSender) {
        if (session == null || session.getEngine() == null || commActionButtons == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        boolean phaseIsComm = session.getEngine().getPhase() == GamePhase.COMMUNICATION;
        boolean showButtons = phaseIsComm || session.getEngine().getPhase() == GamePhase.TRICKING;
        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
        CommunicationToken[] pending = session.getEngine().getPendingTokens();

        for (int i = 0; i < playerCount; i++) {
            if (i < commAreas.length && commAreas[i] != null && i < players.size() && i < commActionButtons.length && commActionButtons[i] != null) {
                Pane commArea = commAreas[i];
                commArea.getChildren().clear();

                Button commButton = commActionButtons[i];
                commArea.getChildren().add(commButton);

                commButton.setManaged(showButtons);
                commButton.setVisible(showButtons);
                updateCommButtonStyle(commButton, i, mission, session, phaseIsComm, communicatingPlayerIndex, showButtons);

                renderTokensForPlayer(commArea, i, mission, pending);
            }
        }

        int localIndex = GameApplication.getPlayerInfo() != null ? GameApplication.getPlayerInfo().getIndex() : -1;
        scheduleDismissTimer(mission, localIndex, actionSender);
    }

    private void updateCommButtonStyle(Button btn, int playerIndex, Mission mission, GameSession session,
                                        boolean phaseIsComm, int communicatingPlayerIndex, boolean showButtons) {
        if (!showButtons) return;
        boolean alreadyUsed = mission.hasPlayerUsedToken(playerIndex);
        if (alreadyUsed) {
            btn.setStyle("-fx-background-color: red; -fx-cursor: default;");
            btn.setDisable(true);
        } else if (phaseIsComm) {
            if (communicatingPlayerIndex == playerIndex) {
                btn.setStyle("-fx-background-color: red; -fx-cursor: hand;");
                btn.setDisable(false);
            } else if (communicatingPlayerIndex == -1) {
                boolean canComm = !session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, mission).isEmpty();
                btn.setStyle(canComm ? GREEN_HAND_STYLE : "-fx-background-color: grey; -fx-cursor: default;");
                btn.setDisable(!canComm);
            } else {
                btn.setDisable(true);
            }
        } else {
            boolean canComm = !session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, mission).isEmpty();
            boolean alreadyRequested = session.getEngine().getCommunicationManager().isCommunicationRequested(playerIndex);
            if (alreadyRequested) {
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

    public void processCommunicationQueue(List<CommunicationToken> tokens, Pane root, Runnable callback) {
        if (tokens == null || tokens.isEmpty()) {
            if (callback != null) callback.run();
            return;
        }

        SequentialTransition sequence = new SequentialTransition();

        for (CommunicationToken token : tokens) {
            final CommunicationToken t = token;
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                int idx = t.getPlayerIndex();
                if (idx < commAreas.length && commAreas[idx] != null) {
                    CardView cv = new CardView(t.getCard());
                    cv.addToken(t.getPosition());
                    commAreas[idx].getChildren().add(cv);
                }
            });
            sequence.getChildren().add(pause);
        }

        sequence.setOnFinished(e -> {
            if (callback != null) callback.run();
        });

        sequence.play();
    }
}
