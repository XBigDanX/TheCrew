package game.thecrew.model;

import java.util.ArrayList;
import java.util.List;

public class Trick {

    private final List<Card> cards = new ArrayList<>();
    private final List<Integer> playerOrder = new ArrayList<>();
    private int startingPlayer;

    public Trick(int startingPlayer) {
        this.startingPlayer = startingPlayer;
    }

    public void addCard(int playerIndex, Card card) {
        playerOrder.add(playerIndex);
        cards.add(card);
    }

    public boolean isComplete(int playerCount) {
        return cards.size() == playerCount;
    }

    public int resolveWinner() {
        // simple rule first:
        // highest value wins
        int bestIndex = 0;

        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(i).getValue() > cards.get(bestIndex).getValue()) {
                bestIndex = i;
            }
        }

        return playerOrder.get(bestIndex);
    }

    public List<Card> getCards() {
        return cards;
    }
}