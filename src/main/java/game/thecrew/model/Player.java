package game.thecrew.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String name;
    private final List<Card> hand;
    private final List<ActiveMissionTask> taskHand = new ArrayList<>();

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCardToHand(Card card) {
        hand.add(card);
        sortHand();
    }

    public void removeCardFromHand(Card card) {
        hand.remove(card);
    }

    public void addTask(ActiveMissionTask task) {
        taskHand.add(task);
    }

    public List<ActiveMissionTask> getTaskHand() {
        return taskHand;
    }

    public void sortHand() {
        hand.sort((c1, c2) -> {

            int colorCompare =
                    Integer.compare(c1.getColor().ordinal(),
                            c2.getColor().ordinal());

            if (colorCompare != 0) {
                return colorCompare;
            }

            return Integer.compare(c1.getValue(), c2.getValue());
        });
    }

    @Override
    public String toString() {
        return name;
    }
}