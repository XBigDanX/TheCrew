package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.Card;
import game.thecrew.model.CommunicationToken;
import game.thecrew.model.GamePhase;
import game.thecrew.model.Mission;
import game.thecrew.model.Player;
import game.thecrew.ui.CardView;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HandUIManager {

    private final FlowPane[] hands;
    private final StackPane[] commAreas;
    private Button[] commActionButtons;
    private boolean dismissTimerScheduled;

    private static final String GREEN_HAND_STYLE = "-fx-background-color: green; -fx-cursor: hand;";

    public HandUIManager(FlowPane hand0, FlowPane hand1, FlowPane hand2, FlowPane hand3, FlowPane hand4,
                         StackPane commArea0, StackPane commArea1, StackPane commArea2, StackPane commArea3, StackPane commArea4) {
        hands = new FlowPane[]{hand0, hand1, hand2, hand3, hand4};
        commAreas = new StackPane[]{commArea0, commArea1, commArea2, commArea3, commArea4};
    }

    public void initCommButtons(int playerCount, Consumer<Integer> onCommClicked) {
        commActionButtons = new Button[playerCount];
        for (int i = 0; i < playerCount; i++) {
            Button button = new Button();
            button.setMinSize(30, 30);
            button.setMaxSize(30, 30);
            button.setShape(new Circle(15));
            button.setStyle(GREEN_HAND_STYLE);
            button.setManaged(false);
            button.setVisible(false);
            final int index = i;
            button.setOnAction(e -> onCommClicked.accept(index));
            StackPane.setAlignment(button, Pos.CENTER);
            if (commAreas[i] != null) {
                commAreas[i].getChildren().add(button);
            }
            commActionButtons[i] = button;
        }
    }

    public void renderAllHands(GameSession session, int playerCount,
                                BiConsumer<Integer, Card> onCardClicked,
                                BiConsumer<Integer, Card> onCommunicationCardSelected) {
        for (int i = 0; i < playerCount; i++) {
            renderPlayerHand(session, i, onCardClicked, onCommunicationCardSelected);
        }
    }

    private void renderPlayerHand(GameSession session, int playerIndex,
                                   BiConsumer<Integer, Card> onCardClicked,
                                   BiConsumer<Integer, Card> onCommunicationCardSelected) {
        if (playerIndex >= hands.length || hands[playerIndex] == null) return;
        if (session == null || session.getEngine() == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        if (playerIndex >= players.size()) return;

        Player player = players.get(playerIndex);
        FlowPane handPane = hands[playerIndex];
        handPane.getChildren().clear();

        boolean isLocalPlayer = GameApplication.getPlayerInfo() != null && playerIndex == GameApplication.getPlayerInfo().getIndex();
        boolean isMyTurn = isLocalPlayer && session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.getPlayerInfo().getIndex();

        if (isLocalPlayer) {
            int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();
            List<Card> validCommCards = (communicatingPlayerIndex == playerIndex)
                    ? session.getEngine().getCommunicationManager().getValidCommunicationCards(playerIndex, session.getEngine().getCurrentMission())
                    : null;

            for (Card card : player.getHand()) {
                CardView cardView = new CardView(card);

                GamePhase currentPhase = session.getEngine().getPhase();
                if (currentPhase == GamePhase.COMMUNICATION && communicatingPlayerIndex == playerIndex) {
                    if (validCommCards != null && validCommCards.contains(card)) {
                        Card clickedCard = card;
                        cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                        cardView.setOnMouseClicked(e -> onCommunicationCardSelected.accept(playerIndex, clickedCard));
                    }
                } else if (isMyTurn && currentPhase == GamePhase.TRICKING) {
                    if (validCommCards != null && validCommCards.contains(card)) {
                        Card clickedCard = card;
                        cardView.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
                        cardView.setOnMouseClicked(e -> onCommunicationCardSelected.accept(playerIndex, clickedCard));
                    } else if (communicatingPlayerIndex == -1) {
                        Card clickedCard = card;
                        cardView.setOnMouseClicked(e -> onCardClicked.accept(playerIndex, clickedCard));
                    }
                }

                handPane.getChildren().add(cardView);
            }
        } else {
            for (int i = 0; i < player.getHand().size(); i++) {
                handPane.getChildren().add(CardView.createBack());
            }
        }
    }

    public void renderCommunicationUI(GameSession session, int playerCount, Consumer<Integer> onDismiss) {
        if (session == null || session.getEngine() == null || commActionButtons == null) return;
        Mission mission = session.getEngine().getCurrentMission();
        if (mission == null) return;

        List<Player> players = session.getEngine().getPlayerManager().getPlayers();
        boolean phaseIsComm = session.getEngine().getPhase() == GamePhase.COMMUNICATION;
        boolean phaseIsTricking = session.getEngine().getPhase() == GamePhase.TRICKING;
        boolean showButtons = phaseIsComm || phaseIsTricking;
        int communicatingPlayerIndex = session.getEngine().getCommunicationManager().getCommunicationPlayerIndex();

        for (int i = 0; i < playerCount; i++) {
            if (i >= commAreas.length || commAreas[i] == null) continue;
            Pane commArea = commAreas[i];
            commArea.getChildren().clear();

            if (i >= players.size() || i >= commActionButtons.length) continue;
            Button commButton = commActionButtons[i];
            if (commButton == null) continue;
            commArea.getChildren().add(commButton);

            commButton.setManaged(showButtons);
            commButton.setVisible(showButtons);
            if (showButtons) {
                boolean alreadyUsed = mission.hasPlayerUsedToken(i);
                if (alreadyUsed) {
                    commButton.setStyle("-fx-background-color: red; -fx-cursor: default;");
                    commButton.setDisable(true);
                } else if (phaseIsComm) {
                    if (communicatingPlayerIndex == i) {
                        commButton.setStyle("-fx-background-color: red; -fx-cursor: hand;");
                        commButton.setDisable(false);
                    } else if (communicatingPlayerIndex == -1) {
                        boolean canComm = !alreadyUsed && !session.getEngine().getCommunicationManager().getValidCommunicationCards(i, mission).isEmpty();
                        commButton.setStyle(canComm ? GREEN_HAND_STYLE : "-fx-background-color: grey; -fx-cursor: default;");
                        commButton.setDisable(!canComm);
                    } else {
                        commButton.setDisable(true);
                    }
                } else {
                    boolean canComm = !alreadyUsed && !session.getEngine().getCommunicationManager().getValidCommunicationCards(i, mission).isEmpty();
                    boolean alreadyRequested = session.getEngine().getCommunicationManager().isCommunicationRequested(i);
                    if (alreadyRequested) {
                        commButton.setStyle("-fx-background-color: orange; -fx-cursor: hand;");
                        commButton.setDisable(false);
                    } else if (canComm) {
                        commButton.setStyle(GREEN_HAND_STYLE);
                        commButton.setDisable(false);
                    } else {
                        commButton.setStyle("-fx-background-color: grey; -fx-cursor: default;");
                        commButton.setDisable(true);
                    }
                }
            }

            for (CommunicationToken token : mission.getActiveTokens()) {
                if (token.getPlayerIndex() == i) {
                    CardView cv = new CardView(token.getCard());
                    cv.addToken(token.getPosition());
                    commArea.getChildren().add(cv);
                }
            }

            CommunicationToken[] pending = session.getEngine().getPendingTokens();
            if (pending != null && pending[i] != null) {
                CardView cv = new CardView(pending[i].getCard());
                cv.addToken(pending[i].getPosition());
                cv.setOpacity(0.6);
                commArea.getChildren().add(cv);
            }
        }

        int localIndex = GameApplication.getPlayerInfo() != null ? GameApplication.getPlayerInfo().getIndex() : -1;
        if (localIndex >= 0 && !dismissTimerScheduled) {
            for (CommunicationToken token : mission.getActiveTokens()) {
                if (token.getPlayerIndex() == localIndex) {
                    dismissTimerScheduled = true;
                    PauseTransition delay = new PauseTransition(Duration.seconds(5));
                    delay.setOnFinished(e -> {
                        onDismiss.accept(localIndex);
                        dismissTimerScheduled = false;
                    });
                    delay.play();
                    break;
                }
            }
        }
    }
}
