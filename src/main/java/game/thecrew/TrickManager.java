package game.thecrew;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.Trick;
import game.thecrew.model.TrickPlay;

import java.util.List;

public class TrickManager {

    private Trick currentTrick = new Trick();

    public boolean playCard(int playerIndex, Card card, List<Card> playerHand) {

        if (!isValidPlay(card, playerHand)) {
            return false;
        }

        currentTrick.addPlay(new TrickPlay(playerIndex, card));
        return true;
    }

    public boolean isComplete(int playerCount) {
        return currentTrick.getPlays().size() == playerCount;
    }

    public int getWinner() {
        return currentTrick.getWinnerIndex(currentTrick.getLeadSuit());
    }

    public CardColor getLeadSuit() {
        return currentTrick.getLeadSuit();
    }

    public void reset() {
        currentTrick = new Trick();
    }

    // =========================
    // VALIDATION
    // =========================
    private boolean isValidPlay(Card card, List<Card> playerHand) {

        CardColor leadSuit = currentTrick.getLeadSuit();

        // submarine (trump) always allowed
        if (card.isTrump()) {
            return true;
        }

        // first card sets lead
        if (leadSuit == null) {
            return true;
        }

        // following suit
        if (card.getColor() == leadSuit) {
            return true;
        }

        // must follow suit if possible
        for (Card c : playerHand) {
            if (!c.isTrump() && c.getColor() == leadSuit) {
                return false;
            }
        }

        return true;
    }
}