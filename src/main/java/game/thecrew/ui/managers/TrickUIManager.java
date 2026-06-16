package game.thecrew.ui.managers;

import game.thecrew.GameSession;
import game.thecrew.model.Trick;
import game.thecrew.model.TrickPlay;
import game.thecrew.ui.CardView;
import javafx.scene.layout.Pane;

public class TrickUIManager {

    private final Pane[] slots;

    public TrickUIManager(Pane slot0, Pane slot1, Pane slot2, Pane slot3, Pane slot4) {
        slots = new Pane[]{slot0, slot1, slot2, slot3, slot4};
    }

    public void clearTrickSlots() {
        for (Pane slot : slots) {
            if (slot != null) {
                slot.getChildren().clear();
            }
        }
    }

    public void renderCurrentTrick(GameSession session) {
        if (session == null || session.getEngine() == null) return;
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
}
