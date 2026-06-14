package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;
import java.util.Set;

public class WinSpecificCardRule implements TaskRule {

    private final Set<Card> targetCards;

    public WinSpecificCardRule(Card targetCard) {
        this.targetCards = Set.of(targetCard);
    }

    public WinSpecificCardRule(Card... cards) {
        this.targetCards = Set.of(cards);
    }

    @Override
    public boolean isTrickBased() {
        return targetCards.size() <= 1;
    }

    @Override
    public boolean checkTrick(Mission mission, Trick trick, int winner) {
        return trick.getPlays().stream()
                .anyMatch(p -> targetCards.contains(p.getCard()));
    }

    @Override
    public boolean checkMissionEnd(Mission mission, int playerIndex) {
        if (targetCards.size() <= 1) return true;
        List<Card> wonCards = mission.getCardsWonByPlayer(playerIndex);
        return wonCards.containsAll(targetCards);
    }
}
