package game.thecrew.ui.managers;

import game.thecrew.GameSession;
import game.thecrew.model.Card;
import game.thecrew.model.Trick;
import game.thecrew.model.TrickPlay;
import game.thecrew.ui.CardView;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TrickUIManager {

    private final Pane[] slots;

    public TrickUIManager(Pane slot0, Pane slot1, Pane slot2, Pane slot3, Pane slot4) {
        this.slots = new Pane[]{slot0, slot1, slot2, slot3, slot4};
    }

    public void clearTrickSlots() {
        for (Pane slot : slots) {
            if (slot != null) {
                slot.getChildren().clear();
            }
        }
    }

    public boolean isTrickPaneEmpty(GameSession session) {
        for (Pane slot : slots) {
            if (slot != null && !slot.getChildren().isEmpty()) {
                return false;
            }
        }
        if (session != null && session.getEngine() != null
                && session.getEngine().getTrickManager() != null
                && session.getEngine().getTrickManager().getCurrentTrick() != null
                && !session.getEngine().getTrickManager().getCurrentTrick().getPlays().isEmpty()) {
            return false;
        }
        return true;
    }

    public void renderCurrentTrick(GameSession session) {
        if (session == null || session.getEngine() == null) return;

        clearTrickSlots();
        Trick currentTrick = session.getEngine().getTrickManager().getCurrentTrick();
        if (currentTrick == null) return;

        for (TrickPlay trickPlay : currentTrick.getPlays()) {
            int playerIndex = trickPlay.getPlayerIndex();
            if (playerIndex >= 0 && playerIndex < slots.length) {
                Pane slot = slots[playerIndex];
                if (slot != null) {
                    slot.getChildren().add(new CardView(trickPlay.getCard()));
                }
            }
        }
    }

    public void animateCardToSlot(Card card, int playerIndex, Pane sourcePane) {
        Pane targetSlot = slots[playerIndex];
        if (targetSlot == null) return;

        if (targetSlot.getScene() == null) return;

        Pane parent = (Pane) targetSlot.getScene().getRoot();
        if (parent == null) return;

        CardView animatingCard = new CardView(card);

        Point2D startInScene = sourcePane.localToScene(sourcePane.getWidth() / 2, sourcePane.getHeight() / 2);
        Point2D endInScene = targetSlot.localToScene(0, 0);

        Point2D startInParent = parent.sceneToLocal(startInScene);
        Point2D endInParent = parent.sceneToLocal(endInScene);

        animatingCard.setLayoutX(startInParent.getX());
        animatingCard.setLayoutY(startInParent.getY());
        parent.getChildren().add(animatingCard);

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), animatingCard);
        tt.setToX(endInParent.getX() - startInParent.getX());
        tt.setToY(endInParent.getY() - startInParent.getY());

        tt.setOnFinished(e -> {
            parent.getChildren().remove(animatingCard);
            targetSlot.getChildren().clear();
            targetSlot.getChildren().add(new CardView(card));
        });

        tt.play();
    }

    public void animateTrickEnd(int winnerIndex, Runnable onFinished) {
        Pane winnerSlot = slots[winnerIndex];
        if (winnerSlot == null) {
            clearTrickSlots();
            if (onFinished != null) onFinished.run();
            return;
        }

        if (winnerSlot.getScene() == null) {
            clearTrickSlots();
            if (onFinished != null) onFinished.run();
            return;
        }

        Pane parent = (Pane) winnerSlot.getScene().getRoot();
        Point2D winnerInScene = winnerSlot.localToScene(0, 0);
        Point2D winnerInParent = parent.sceneToLocal(winnerInScene);

        List<Node> animatedCards = new ArrayList<>();
        ParallelTransition parallel = new ParallelTransition();

        for (Pane slot : slots) {
            if (slot == null || slot.getChildren().isEmpty()) continue;

            Node cardNode = slot.getChildren().get(0);
            Point2D slotInScene = slot.localToScene(0, 0);
            Point2D slotInParent = parent.sceneToLocal(slotInScene);

            // Prebacivanje karte na roditelja radi animacije
            slot.getChildren().clear();
            cardNode.setLayoutX(slotInParent.getX());
            cardNode.setLayoutY(slotInParent.getY());
            parent.getChildren().add(cardNode);
            animatedCards.add(cardNode);

            TranslateTransition tt = new TranslateTransition(Duration.millis(600), cardNode);
            tt.setToX(winnerInParent.getX() - slotInParent.getX());
            tt.setToY(winnerInParent.getY() - slotInParent.getY());
            parallel.getChildren().add(tt);
        }

        parallel.setOnFinished(e -> {
            parent.getChildren().removeAll(animatedCards);
            clearTrickSlots();
            if (onFinished != null) onFinished.run();
        });

        parallel.play();
    }
}