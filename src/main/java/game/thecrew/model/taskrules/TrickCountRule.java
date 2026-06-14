package game.thecrew.model.taskrules;

import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;

public class TrickCountRule implements TaskRule {

    public enum Type {
        MORE_THAN_EVERYONE,
        FEWER_THAN_CAPTAIN,
        FIRST_TRICK,
        LAST_TRICK,
        FIRST_AND_LAST,
        EXACTLY_N_TRICKS,
        DONT_WIN_ANY,
        N_CONSECUTIVE
    }

    private final Type type;
    private final int parameter;

    public TrickCountRule(Type type) {
        this(type, 0);
    }

    public TrickCountRule(Type type, int parameter) {
        this.type = type;
        this.parameter = parameter;
    }

    @Override
    public boolean isTrickBased() {
        return type == Type.FIRST_TRICK;
    }

    @Override
    public boolean checkTrick(Mission mission, Trick trick, int winner) {
        if (type == Type.FIRST_TRICK) {
            return mission.getCompletedTricksCount() == 1;
        }
        return false;
    }

    @Override
    public boolean checkMissionEnd(Mission mission, int playerIndex) {
        int myCount = mission.getPlayerWinCount(playerIndex);
        int playerCount = mission.getPlayerCount();

        return switch (type) {
            case MORE_THAN_EVERYONE -> {
                for (int i = 0; i < playerCount; i++) {
                    if (i != playerIndex && mission.getPlayerWinCount(i) >= myCount) {
                        yield false;
                    }
                }
                yield true;
            }
            case FEWER_THAN_CAPTAIN -> {
                int captainCount = mission.getPlayerWinCount(mission.getCaptainIndex());
                yield myCount < captainCount;
            }
            case FIRST_TRICK -> mission.getCompletedTricksCount() == 1;
            case LAST_TRICK -> checkLastTrick(mission, playerIndex);
            case FIRST_AND_LAST -> checkFirstAndLast(mission, playerIndex);
            case EXACTLY_N_TRICKS -> myCount == parameter;
            case DONT_WIN_ANY -> myCount == 0;
            case N_CONSECUTIVE -> checkConsecutive(mission, playerIndex) >= parameter;
        };
    }

    private boolean checkLastTrick(Mission mission, int playerIndex) {
        List<Trick> tricks = mission.getCompletedTricks();
        if (tricks.isEmpty()) return false;
        Trick last = tricks.get(tricks.size() - 1);
        return last.getWinnerIndex(last.getLeadSuit()) == playerIndex;
    }

    private boolean checkFirstAndLast(Mission mission, int playerIndex) {
        List<Trick> tricks = mission.getCompletedTricks();
        if (tricks.size() < 2) return false;
        Trick first = tricks.get(0);
        Trick last = tricks.get(tricks.size() - 1);
        return first.getWinnerIndex(first.getLeadSuit()) == playerIndex
                && last.getWinnerIndex(last.getLeadSuit()) == playerIndex;
    }

    private int checkConsecutive(Mission mission, int playerIndex) {
        List<Trick> tricks = mission.getCompletedTricks();
        int maxStreak = 0;
        int currentStreak = 0;
        for (Trick trick : tricks) {
            boolean won = trick.getWinnerIndex(trick.getLeadSuit()) == playerIndex;
            if (won) {
                currentStreak++;
                if (currentStreak > maxStreak) maxStreak = currentStreak;
            } else {
                currentStreak = 0;
            }
        }
        return maxStreak;
    }

}
