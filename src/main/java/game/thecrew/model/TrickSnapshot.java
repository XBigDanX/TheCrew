package game.thecrew.model;

import java.util.List;

public class TrickSnapshot {

    private final List<TrickPlay> plays;
    private final int winnerIndex;
    private final CardColor leadSuit;
    private final int trickNumber;

    public TrickSnapshot(List<TrickPlay> plays, int winnerIndex, CardColor leadSuit, int trickNumber) {
        this.plays = List.copyOf(plays);
        this.winnerIndex = winnerIndex;
        this.leadSuit = leadSuit;
        this.trickNumber = trickNumber;
    }

    public List<TrickPlay> getPlays() {
        return plays;
    }

    public int getWinnerIndex() {
        return winnerIndex;
    }

    public CardColor getLeadSuit() {
        return leadSuit;
    }

    public int getTrickNumber() {
        return trickNumber;
    }
}
