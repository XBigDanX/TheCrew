package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;

public class LastTrickWinRule implements TaskRule {

    private final Card targetCard;

    public LastTrickWinRule(Card targetCard) {
        this.targetCard = targetCard;
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
        List<Trick> tricks = mission.getCompletedTricks();
        if (tricks.isEmpty()) return false;
        Trick last = tricks.get(tricks.size() - 1);
        if (last.getWinnerIndex(last.getLeadSuit()) != playerIndex) return false;
        return last.getPlays().stream().anyMatch(p -> p.getCard().equals(targetCard));
    }
}
