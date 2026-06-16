package game.thecrew.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {

    private final String name;
    private final List<Card> hand;
    private final List<Task> taskHand = new ArrayList<>();

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

    public void addTask(Task task) {
        taskHand.add(task);
    }

    public List<Task> getTaskHand() {
        return taskHand;
    }

    public void sortHand() {
        hand.sort((card1, card2) -> {
            int colorComparison = Integer.compare(card1.getColor().ordinal(), card2.getColor().ordinal());
            if (colorComparison != 0) {
                return colorComparison;
            }
            return Integer.compare(card1.getValue(), card2.getValue());
        });
    }

    @Override
    public String toString() {
        return name;
    }
}