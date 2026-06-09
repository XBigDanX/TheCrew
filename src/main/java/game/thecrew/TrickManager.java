package game.thecrew;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.TrickPlay;
import java.util.ArrayList;
import java.util.List;

public class TrickManager {
    private final List<TrickPlay> plays = new ArrayList<>();

    public boolean playCard(int playerIndex, Card card, CardColor leadSuit, List<Card> playerHand) {
        if (leadSuit != null && card.getColor() != leadSuit) {
            for (Card c : playerHand) {
                if (c.getColor() == leadSuit) {
                    return false;
                }
            }
        }

        plays.add(new TrickPlay(playerIndex, card));
        return true;
    }

    public boolean isTrickComplete(int playerCount) {
        return plays.size() == playerCount;
    }

    public int determineWinner(CardColor leadSuit) {
        int winnerIndex = -1;
        Card winningCard = null;

        for (TrickPlay play : plays) {
            Card card = play.getCard();

            if (winningCard == null) {
                winnerIndex = play.getPlayerIndex();
                winningCard = card;
            } else {
                if (card.isTrump()) {
                    if (!winningCard.isTrump() || card.getValue() > winningCard.getValue()) {
                        winnerIndex = play.getPlayerIndex();
                        winningCard = card;
                    }
                } else if (!winningCard.isTrump() && card.getColor() == leadSuit) {
                    if (winningCard.getColor() != leadSuit || card.getValue() > winningCard.getValue()) {
                        winnerIndex = play.getPlayerIndex();
                        winningCard = card;
                    }
                }
            }
        }
        return winnerIndex;
    }

    public void clearTrick() {
        plays.clear();
    }

    public CardColor getLeadSuit() {
        if (plays.isEmpty()) return null;
        return plays.get(0).getCard().getColor();
    }
}
