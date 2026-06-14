package game.thecrew.model.taskrules;

import game.thecrew.model.Card;
import game.thecrew.model.CardColor;
import game.thecrew.model.Mission;
import game.thecrew.model.TaskRule;
import game.thecrew.model.Trick;

import java.util.List;
import java.util.function.Predicate;

public class CollectCardsRule implements TaskRule {

    public enum Type { EXACTLY, AT_LEAST, AT_MOST }

    public static final class Criterion {
        private final Predicate<Card> filter;
        private final int count;
        private final Type type;

        public Criterion(Predicate<Card> filter, int count, Type type) {
            this.filter = filter;
            this.count = count;
            this.type = type;
        }

        public Predicate<Card> filter() { return filter; }
        public int count() { return count; }
        public Type type() { return type; }
    }

    private final List<Criterion> criteria;

    public CollectCardsRule(Predicate<Card> filter, int count, Type type) {
        this(List.of(new Criterion(filter, count, type)));
    }

    public CollectCardsRule(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    @SafeVarargs
    public static CollectCardsRule compound(Criterion... criteria) {
        return new CollectCardsRule(List.of(criteria));
    }

    public static CollectCardsRule byColor(CardColor color, int count, Type type) {
        return new CollectCardsRule(card -> !card.isTrump() && card.getColor() == color, count, type);
    }

    public static CollectCardsRule submarines(int count, Type type) {
        return new CollectCardsRule(Card::isTrump, count, type);
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
        for (Criterion c : criteria) {
            long matched = wonCards.stream().filter(c.filter()).count();
            if (!checkCount(matched, c.count(), c.type())) return false;
        }
        return true;
    }

    private boolean checkCount(long actual, int expected, Type t) {
        return switch (t) {
            case EXACTLY -> actual == expected;
            case AT_LEAST -> actual >= expected;
            case AT_MOST -> actual <= expected;
        };
    }
}
