package game.thecrew.model;

public class Card {

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
    public String toString() {
        return color + " " + value;
    }
}