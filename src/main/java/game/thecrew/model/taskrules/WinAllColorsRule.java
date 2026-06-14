package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;

public class WinAllColorsRule implements TaskRule {

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
        return wonCards.stream()
                .filter(c -> !c.isTrump())
                .map(Card::getColor)
                .distinct()
                .count() == 4;
    }
}
