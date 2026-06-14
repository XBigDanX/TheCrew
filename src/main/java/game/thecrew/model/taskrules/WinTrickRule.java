package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;
import game.thecrew.model.TrickPlay;

import java.util.List;

public class WinTrickRule implements TaskRule {

    @FunctionalInterface
    public interface TrickCondition {
        boolean matches(Trick trick, Mission mission);
    }

    private final TrickCondition condition;

    public WinTrickRule(TrickCondition condition) {
        this.condition = condition;
    }

    public static WinTrickRule containsCardOfValue(int value) {
        return new WinTrickRule((trick, mission) ->
                trick.getPlays().stream().anyMatch(p -> p.getCard().getValue() == value));
    }

    public static WinTrickRule allCardsInRange(int minValue, int maxValue, boolean excludeSubs) {
        return new WinTrickRule((trick, mission) -> {
            for (TrickPlay play : trick.getPlays()) {
                Card card = play.getCard();
                if (excludeSubs && card.isTrump()) continue;
                if (card.getValue() < minValue || card.getValue() > maxValue) return false;
            }
            return true;
        });
    }

    public static WinTrickRule allCardsLowerThan(int maxExclusive, boolean excludeSubs) {
        return allCardsInRange(1, maxExclusive - 1, excludeSubs);
    }

    public static WinTrickRule allCardsGreaterThan(int minExclusive, boolean excludeSubs) {
        return allCardsInRange(minExclusive + 1, 9, excludeSubs);
    }

    public static WinTrickRule parity(boolean even, boolean excludeSubs) {
        return new WinTrickRule((trick, mission) -> {
            for (TrickPlay play : trick.getPlays()) {
                Card card = play.getCard();
                if (excludeSubs && card.isTrump()) continue;
                boolean isEven = card.getValue() % 2 == 0;
                if (isEven != even) return false;
            }
            return true;
        });
    }

    public static WinTrickRule sumLowerThan(int threshold, boolean excludeSubs) {
        return new WinTrickRule((trick, mission) -> sumCards(trick, excludeSubs) < threshold);
    }

    public static WinTrickRule sumGreaterThan(int p3threshold, int p4threshold, int p5threshold, boolean excludeSubs) {
        return new WinTrickRule((trick, mission) -> {
            int threshold = switch (mission.getPlayerCount()) {
                case 3 -> p3threshold;
                case 4 -> p4threshold;
                default -> p5threshold;
            };
            return sumCards(trick, excludeSubs) > threshold;
        });
    }

    // — Helpers —

    private static int sumCards(Trick trick, boolean excludeSubs) {
        int sum = 0;
        for (TrickPlay play : trick.getPlays()) {
            Card card = play.getCard();
            if (excludeSubs && card.isTrump()) continue;
            sum += card.getValue();
        }
        return sum;
    }

    @Override
    public boolean isTrickBased() {
        return true;
    }

    @Override
    public boolean checkTrick(Mission mission, Trick trick, int winner) {
        return condition.matches(trick, mission);
    }

    @Override
    public boolean checkMissionEnd(Mission mission, int playerIndex) {
        return false;
    }
}
