package game.thecrew.model;

import java.io.Serializable;

public class TrickPlay implements Serializable {

    private final int playerIndex;
    private final Card card;

    public TrickPlay(int playerIndex, Card card) {
        this.playerIndex = playerIndex;
        this.card = card;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public Card getCard() {
        return card;
    }
}
