package game.thecrew;

import game.thecrew.model.*;

public class TrickManager {

    private Trick currentTrick = new Trick();

    // =========================
    // PLAY CARD
    // =========================
    public void playCard(int playerIndex, Card card) {
        currentTrick.addPlay(new TrickPlay(playerIndex, card));
    }

    // =========================
    // STATE
    // =========================
    public boolean isTrickComplete(int playerCount) {
        return currentTrick.getPlays().size() == playerCount;
    }

    public CardColor getLeadSuit() {
        return currentTrick.getLeadSuit();
    }

    // =========================
    // WINNER
    // =========================
    public int determineWinner() {
        return currentTrick.getWinnerIndex(getLeadSuit());
    }

    // =========================
    // RESET
    // =========================
    public void clearTrick() {
        currentTrick = new Trick();
    }
}