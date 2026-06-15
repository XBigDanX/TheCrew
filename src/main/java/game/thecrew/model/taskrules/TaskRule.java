package game.thecrew.model.taskrules;

import game.thecrew.model.Mission;
import game.thecrew.model.Trick;

import java.io.Serializable;

public interface TaskRule extends Serializable {

    boolean isTrickBased();

    boolean checkTrick(Mission mission, Trick trick, int winner);

    boolean checkMissionEnd(Mission mission, int playerIndex);
}
