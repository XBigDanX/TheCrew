package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.Card;
import game.thecrew.model.GamePhase;
import game.thecrew.model.Player;
import game.thecrew.ui.CardView;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.function.BiConsumer;

public class HandUIManager {

    private final FlowPane[] hands;

    public HandUIManager(FlowPane hand0, FlowPane hand1, FlowPane hand2, FlowPane hand3, FlowPane hand4) {
        hands = new FlowPane[]{hand0, hand1, hand2, hand3, hand4};
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

        boolean isLocalPlayer = GameApplication.playerInfo != null && playerIndex == GameApplication.playerInfo.getIndex();
        boolean isMyTurn = isLocalPlayer && session.getEngine().getPlayerManager().getCurrentPlayerIndex() == GameApplication.playerInfo.getIndex();

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
}
