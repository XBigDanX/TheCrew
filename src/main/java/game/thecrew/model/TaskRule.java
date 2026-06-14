package game.thecrew.model;

import java.io.Serializable;

public interface TaskRule extends Serializable {

    boolean isTrickBased();

    boolean checkTrick(Mission mission, Trick trick, int winner);

    boolean checkMissionEnd(Mission mission, int playerIndex);
}
