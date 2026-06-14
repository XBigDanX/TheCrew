package game.thecrew.model.taskrules;

import game.thecrew.model.CardColor;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;
import game.thecrew.model.TrickPlay;

import java.util.Set;

public class AvoidLeadRule implements TaskRule {

    private final Set<CardColor> forbiddenColors;

    public AvoidLeadRule(CardColor... colors) {
        this.forbiddenColors = Set.of(colors);
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
        for (Trick trick : mission.getCompletedTricks()) {
            if (trick.getPlays().isEmpty()) continue;
            TrickPlay lead = trick.getPlays().get(0);
            if (lead.getPlayerIndex() == playerIndex && forbiddenColors.contains(lead.getCard().getColor())) {
                return false;
            }
        }
        return true;
    }
}
