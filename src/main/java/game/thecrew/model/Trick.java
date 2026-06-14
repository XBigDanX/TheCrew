package game.thecrew.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trick implements Serializable {

    private final List<TrickPlay> plays = new ArrayList<>();

    public void addPlay(TrickPlay play) {
        plays.add(play);
    }

    public List<TrickPlay> getPlays() {
        return plays;
    }

    public CardColor getLeadSuit() {
        if (plays.isEmpty()) return null;
        return plays.get(0).getCard().getColor();
    }

    public int getWinnerIndex(CardColor leadSuit) {

        int winnerIndex = -1;
        Card winningCard = null;

        for (TrickPlay play : plays) {

            Card card = play.getCard();

            if (winningCard == null) {
                winnerIndex = play.getPlayerIndex();
                winningCard = card;
                continue;
            }

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

        return winnerIndex;
    }

    public TrickPlay getPlayerPlay(int playerIndex) {
        for (TrickPlay play : plays) {
            if (play.getPlayerIndex() == playerIndex) {
                return play;
            }
        }
        return null;
    }
}