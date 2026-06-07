package game.thecrew.ui;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class PlayerUI {
    private final FlowPane hand;
    private final Pane slot;
    private final HBox taskHand;

    public PlayerUI(FlowPane hand, Pane slot, HBox taskHand) {
        this.hand = hand;
        this.slot = slot;
        this.taskHand = taskHand;
    }

    public FlowPane getHand() { return hand; }
    public Pane getSlot() { return slot; }
    public HBox getTaskHand() { return taskHand; }

    public void setVisible(boolean visible) {
        if (hand != null) {
            hand.setVisible(visible);
            hand.setManaged(visible);
        }
        if (slot != null) {
            slot.setVisible(visible);
            slot.setManaged(visible);
        }
        if (taskHand != null) {
            taskHand.setVisible(visible);
            taskHand.setManaged(visible);
        }
    }
}