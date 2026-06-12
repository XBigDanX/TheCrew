package game.thecrew.model;

public interface TaskRule {

    boolean isTrickBased();

    boolean checkTrick(Mission mission, Trick trick, int winner);

    boolean checkMissionEnd(Mission mission, int playerIndex);
}
