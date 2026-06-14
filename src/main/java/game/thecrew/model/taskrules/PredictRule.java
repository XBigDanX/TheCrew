package game.thecrew.model.taskrules;

import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

public class PredictRule implements TaskRule {

    private final int prediction;

    public PredictRule(int prediction, boolean hidden) {
        this.prediction = prediction;
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
        return mission.getPlayerWinCount(playerIndex) == prediction;
    }
}
