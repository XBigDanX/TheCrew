package game.thecrew.ui;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class CardView extends StackPane {

    private final Card card;

    public CardView(Card card) {

        this.card = card;

        Rectangle background = new Rectangle(40, 60);

        background.setFill(getColor(card.getColor()));

        Text text = new Text(String.valueOf(card.getValue()));

        getChildren().addAll(background, text);
    }

    private Color getColor(CardColor color) {

        return switch (color) {
            case BLUE -> Color.LIGHTBLUE;
            case GREEN -> Color.LIGHTGREEN;
            case YELLOW -> Color.KHAKI;
            case RED -> Color.SALMON;
            case SUBMARINE -> Color.GRAY;
        };
    }

    public Card getCard() {
        return card;
    }
}