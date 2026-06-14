package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;

public class ColorComparisonRule implements TaskRule {

    public enum Comparison { MORE, SAME }

    private final CardColor colorA;
    private final CardColor colorB;
    private final Comparison comparison;
    private final boolean bothPositive;

    public ColorComparisonRule(CardColor colorA, CardColor colorB, Comparison comparison) {
        this(colorA, colorB, comparison, false);
    }

    public ColorComparisonRule(CardColor colorA, CardColor colorB, Comparison comparison, boolean bothPositive) {
        this.colorA = colorA;
        this.colorB = colorB;
        this.comparison = comparison;
        this.bothPositive = bothPositive;
    }

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
        long countA = wonCards.stream().filter(c -> c.getColor() == colorA).count();
        long countB = wonCards.stream().filter(c -> c.getColor() == colorB).count();

        return switch (comparison) {
            case MORE -> countA > countB;
            case SAME -> bothPositive ? (countA == countB && countA > 0) : countA == countB;
        };
    }
}
