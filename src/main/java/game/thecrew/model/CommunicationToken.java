package game.thecrew.model;

import java.io.Serializable;

public class CommunicationToken implements Serializable {
    private final Card card;
    private final TokenPosition position;
    private final int playerIndex;

    public CommunicationToken(Card card, TokenPosition position, int playerIndex) {
        this.card = card;
        this.position = position;
        this.playerIndex = playerIndex;
    }

    public Card getCard() {
        return card;
    }

    public TokenPosition getPosition() {
        return position;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }
}
