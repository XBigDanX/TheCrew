package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;
import java.util.stream.Collectors;

public class WinEntireColorRule implements TaskRule {

    @Override
    public boolean isTrickBased() {
        return false;
    }

    @Override
    public boolean checkTrick(Mission mission, Trick trick, int winner) {
        return false;
    }

    @Override
    public boolean checkMissionEnd(Mission mission, int playerIndex) {
        List<Card> wonCards = mission.getCardsWonByPlayer(playerIndex);
        List<Card> nonTrump = wonCards.stream().filter(c -> !c.isTrump()).collect(Collectors.toList());

        for (CardColor color : CardColor.values()) {
            if (color == CardColor.SUBMARINE) continue;
            long count = nonTrump.stream().filter(c -> c.getColor() == color).count();
            if (count == 9) return true;
        }
        return false;
    }
}
