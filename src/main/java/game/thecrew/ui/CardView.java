package game.thecrew.ui;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.TokenPosition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

    public void addToken(TokenPosition position) {
        Circle token = new Circle(5, Color.YELLOW);
        token.setStroke(Color.BLACK);
        StackPane.setMargin(token, new javafx.geometry.Insets(2, 2, 2, 2));

        switch (position) {
            case TOP -> StackPane.setAlignment(token, Pos.TOP_CENTER);
            case MIDDLE -> StackPane.setAlignment(token, Pos.CENTER);
            case BOTTOM -> StackPane.setAlignment(token, Pos.BOTTOM_CENTER);
        }
        getChildren().add(token);
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