package game.thecrew.model;

import java.io.Serializable;

public class Card implements Serializable {

    private final CardColor color;
    private final int value;

    public Card(CardColor color, int value) {
        this.color = color;
        this.value = value;
    }

    public CardColor getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    public boolean isTrump() {
        return color == CardColor.SUBMARINE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return value == card.value && color == card.color;
    }

    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + value;
        return result;
    }

    @Override
    public String toString() {
        return color + " " + value;
    }
}