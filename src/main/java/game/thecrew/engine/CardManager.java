package game.thecrew.engine;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardManager {
    public List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        for (CardColor color : CardColor.values()) {
            if (color == CardColor.SUBMARINE) {
                for (int i = 1; i <= 4; i++) deck.add(new Card(color, i));
            } else {
                for (int i = 1; i <= 9; i++) deck.add(new Card(color, i));
            }
        }
        return deck;
    }

    public void dealCards(List<Player> players) {
        List<Card> deck = createDeck();
        Collections.shuffle(deck, new Random());
        int playerIndex = 0;
        while (!deck.isEmpty()) {
            players.get(playerIndex).addCardToHand(deck.remove(deck.size() - 1));
            playerIndex = (playerIndex + 1) % players.size();
        }
    }

    public int determineCaptain(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            for (Card card : players.get(i).getHand()) {
                if (card.getColor() == CardColor.SUBMARINE && card.getValue() == 4) {
                    return i;
                }
            }
        }
        return 0;
    }
}
