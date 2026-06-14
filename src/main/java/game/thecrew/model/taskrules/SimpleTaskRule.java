package game.thecrew.model.taskrules;

import game.thecrew.model.*;

import java.io.Serializable;
import java.util.List;

public class SimpleTaskRule implements TaskRule {

    public enum RuleType {
        SPECIFIC_CARD,       // Win Color X Value Y
        ANY_OF_COLOR,        // Win any card of Color X
        ANY_OF_VALUE,        // Win any card of Value Y
        TRICK_COUNT,         // Win exactly N tricks, or no tricks
        FIRST_TRICK,         // Win the first trick
        LAST_TRICK,          // Win the last trick
        COLLECT_ALL_COLORS,  // Win at least one card of each of the 4 colors
        COLLECT_COUNT,       // Win N cards of specific color (or submarine if color null)
        CONSECUTIVE_TRICKS,  // Win N consecutive tricks
        FEWER_THAN_CAPTAIN,  // Win fewer tricks than the captain
        MORE_THAN_EVERYONE,  // Win more tricks than any other player
        COLOR_EQUAL,         // Win same number of cards of color X and Y
        SUM_LESS_THAN,       // Win a trick with sum < X
        SUM_GREATER_THAN,    // Win a trick with sum > X
        PARITY,               // Win trick with all cards of specific parity
        ALL_IN_RANGE,         // Win trick with all cards in [min, max]
        ENTIRE_COLOR,         // Win all 9 cards of any non-submarine color
        AVOID_LEAD,           // Never lead a trick with specific color(s)
        COLOR_COMPARISON,     // More cards of color X than Y
        LAST_TRICK_WITH_CARD, // Win the last trick containing specific card
        FIRST_AND_LAST        // Win both first and last trick
    }

    private final RuleType type;
    private final CardColor color;
    private final int value;
    private final int parameter; // used for count, threshold, etc.
    private final int parameter2; // used for ranges

    public SimpleTaskRule(RuleType type, CardColor color, int value, int parameter, int parameter2) {
        this.type = type;
        this.color = color;
        this.value = value;
        this.parameter = parameter;
        this.parameter2 = parameter2;
    }

    public SimpleTaskRule(RuleType type, CardColor color, int value, int parameter) {
        this(type, color, value, parameter, 0);
    }

    // Static factory methods for convenience
    public static SimpleTaskRule winSpecificCard(CardColor color, int value) {
        return new SimpleTaskRule(RuleType.SPECIFIC_CARD, color, value, 0);
    }

    public static SimpleTaskRule winAnyOfColor(CardColor color) {
        return new SimpleTaskRule(RuleType.ANY_OF_COLOR, color, 0, 0);
    }

    public static SimpleTaskRule winAnyOfValue(int value) {
        return new SimpleTaskRule(RuleType.ANY_OF_VALUE, null, value, 0);
    }

    public static SimpleTaskRule trickCount(int count) {
        return new SimpleTaskRule(RuleType.TRICK_COUNT, null, 0, count);
    }

    public static SimpleTaskRule sumLowerThan(int threshold) {
        return new SimpleTaskRule(RuleType.SUM_LESS_THAN, null, 0, threshold);
    }

    public static SimpleTaskRule sumGreaterThan(int threshold) {
        return new SimpleTaskRule(RuleType.SUM_GREATER_THAN, null, 0, threshold);
    }

    public static SimpleTaskRule sumGreaterThan(int threshold3Players, int threshold4Players, int threshold5Players) {
        // We'll store these in parameter, parameter2, and value (hacky but simple)
        return new SimpleTaskRule(RuleType.SUM_GREATER_THAN, null, threshold5Players, threshold3Players, threshold4Players);
    }

    public static SimpleTaskRule firstTrick() {
        return new SimpleTaskRule(RuleType.FIRST_TRICK, null, 0, 0);
    }

    public static SimpleTaskRule lastTrick() {
        return new SimpleTaskRule(RuleType.LAST_TRICK, null, 0, 0);
    }

    public static SimpleTaskRule collectAllColors() {
        return new SimpleTaskRule(RuleType.COLLECT_ALL_COLORS, null, 0, 0);
    }

    public static SimpleTaskRule collectCount(CardColor color, int count) {
        return new SimpleTaskRule(RuleType.COLLECT_COUNT, color, 0, count);
    }

    public static SimpleTaskRule allInRange(int min, int max) {
        return new SimpleTaskRule(RuleType.ALL_IN_RANGE, null, 0, min, max);
    }

    public static SimpleTaskRule allOdd() {
        return new SimpleTaskRule(RuleType.PARITY, null, 0, 1);
    }

    public static SimpleTaskRule allEven() {
        return new SimpleTaskRule(RuleType.PARITY, null, 0, 0);
    }

    public static SimpleTaskRule consecutiveTricks(int count) {
        return new SimpleTaskRule(RuleType.CONSECUTIVE_TRICKS, null, 0, count);
    }

    public static SimpleTaskRule fewerThanCaptain() {
        return new SimpleTaskRule(RuleType.FEWER_THAN_CAPTAIN, null, 0, 0);
    }

    public static SimpleTaskRule entireColor() {
        return new SimpleTaskRule(RuleType.ENTIRE_COLOR, null, 0, 0);
    }

    public static SimpleTaskRule moreThanEveryone() {
        return new SimpleTaskRule(RuleType.MORE_THAN_EVERYONE, null, 0, 0);
    }

    public static SimpleTaskRule colorEqual(CardColor c1, CardColor c2) {
        return new SimpleTaskRule(RuleType.COLOR_EQUAL, c1, 0, c2.ordinal());
    }

    public static SimpleTaskRule avoidLead(CardColor color) {
        return new SimpleTaskRule(RuleType.AVOID_LEAD, color, 0, 0);
    }

    public static SimpleTaskRule lastTrickWithCard(CardColor color, int value) {
        return new SimpleTaskRule(RuleType.LAST_TRICK_WITH_CARD, color, value, 0);
    }

    public static SimpleTaskRule firstAndLast() {
        return new SimpleTaskRule(RuleType.FIRST_AND_LAST, null, 0, 0);
    }

    public static SimpleTaskRule colorComparison(CardColor more, CardColor less) {
        // We use 'color' for 'more' and parameter for 'less' (ordinal)
        return new SimpleTaskRule(RuleType.COLOR_COMPARISON, more, 0, less.ordinal());
    }

    @Override
    public boolean isTrickBased() {
        return type == RuleType.SPECIFIC_CARD || type == RuleType.ANY_OF_COLOR || type == RuleType.ANY_OF_VALUE
                || type == RuleType.SUM_LESS_THAN || type == RuleType.SUM_GREATER_THAN || type == RuleType.PARITY
                || type == RuleType.FIRST_TRICK || type == RuleType.ALL_IN_RANGE || type == RuleType.LAST_TRICK_WITH_CARD;
    }

    @Override
    public boolean checkTrick(Mission mission, Trick trick, int winner) {
        return switch (type) {
            case SPECIFIC_CARD -> trick.getPlays().stream().anyMatch(play -> play.getCard().getColor() == color && play.getCard().getValue() == value);
            case ANY_OF_COLOR -> trick.getPlays().stream().anyMatch(play -> play.getCard().getColor() == color);
            case ANY_OF_VALUE -> trick.getPlays().stream().anyMatch(play -> play.getCard().getValue() == value);
            case SUM_LESS_THAN -> sumCards(trick) < parameter;
            case SUM_GREATER_THAN -> {
                int threshold;
                if (parameter2 == 0 && value == 0) {
                    threshold = parameter;
                } else {
                    threshold = switch (mission.getPlayerCount()) {
                        case 3 -> parameter;
                        case 4 -> parameter2;
                        default -> value;
                    };
                }
                yield sumCards(trick) > threshold;
            }
            case PARITY -> trick.getPlays().stream().allMatch(play -> {
                int cardValue = play.getCard().getValue();
                return (parameter == 0) ? (cardValue % 2 == 0) : (cardValue % 2 != 0);
            });
            case FIRST_TRICK -> mission.getCompletedTricksCount() == 0; // checkTrick is called BEFORE adding to completed
            case ALL_IN_RANGE -> trick.getPlays().stream().allMatch(play -> play.getCard().getValue() >= parameter && play.getCard().getValue() <= parameter2);
            case LAST_TRICK_WITH_CARD -> {
                if (trick.getPlays().stream().anyMatch(play -> play.getCard().getColor() == color && play.getCard().getValue() == value)) {
                    // It MUST be the last trick. If player count is N, total tricks is 40/N.
                    int totalTricks = 40 / mission.getPlayerCount();
                    yield mission.getCompletedTricksCount() == totalTricks - 1;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public boolean checkMissionEnd(Mission mission, int playerIndex) {
        List<Card> wonCards = mission.getCardsWonByPlayer(playerIndex);
        return switch (type) {
            case TRICK_COUNT -> mission.getPlayerWinCount(playerIndex) == parameter;
            case COLLECT_ALL_COLORS -> {
                long colors = wonCards.stream().filter(card -> !card.isTrump()).map(Card::getColor).distinct().count();
                yield colors == 4;
            }
            case COLLECT_COUNT -> {
                long count;
                if (color == null) {
                    count = wonCards.stream().filter(Card::isTrump).count();
                } else {
                    count = wonCards.stream().filter(wonCard -> wonCard.getColor() == color).count();
                }
                yield count >= parameter;
            }
            case CONSECUTIVE_TRICKS -> {
                int maxStreak = 0;
                int currentStreak = 0;
                for (Trick trick : mission.getCompletedTricks()) {
                    if (trick.getWinnerIndex(trick.getLeadSuit()) == playerIndex) {
                        currentStreak++;
                        maxStreak = Math.max(maxStreak, currentStreak);
                    } else {
                        currentStreak = 0;
                    }
                }
                yield maxStreak >= parameter;
            }
            case FEWER_THAN_CAPTAIN -> {
                int captainCount = mission.getPlayerWinCount(mission.getCaptainIndex());
                yield mission.getPlayerWinCount(playerIndex) < captainCount;
            }
            case MORE_THAN_EVERYONE -> {
                int myCount = mission.getPlayerWinCount(playerIndex);
                boolean isMore = true;
                for (int i = 0; i < mission.getPlayerCount(); i++) {
                    if (i != playerIndex && mission.getPlayerWinCount(i) >= myCount) {
                        isMore = false;
                        break;
                    }
                }
                yield isMore;
            }
            case COLOR_EQUAL -> {
                CardColor otherColor = CardColor.values()[parameter];
                long count1 = wonCards.stream().filter(card -> !card.isTrump() && card.getColor() == color).count();
                long count2 = wonCards.stream().filter(card -> !card.isTrump() && card.getColor() == otherColor).count();
                yield count1 > 0 && count1 == count2;
            }
            case LAST_TRICK -> {
                List<Trick> tricks = mission.getCompletedTricks();
                if (tricks.isEmpty()) yield false;
                Trick last = tricks.get(tricks.size() - 1);
                yield last.getWinnerIndex(last.getLeadSuit()) == playerIndex;
            }
            case COLOR_COMPARISON -> {
                CardColor lessColor = CardColor.values()[parameter];
                long moreCount = wonCards.stream().filter(card -> !card.isTrump() && card.getColor() == color).count();
                long lessCount = wonCards.stream().filter(card -> !card.isTrump() && card.getColor() == lessColor).count();
                yield moreCount > lessCount;
            }
            case ENTIRE_COLOR -> {
                for (CardColor c : CardColor.values()) {
                    if (c == CardColor.SUBMARINE) continue;
                    long count = wonCards.stream().filter(card -> !card.isTrump() && card.getColor() == c).count();
                    if (count == 9) yield true;
                }
                yield false;
            }
            case AVOID_LEAD -> {
                boolean leadForbidden = false;
                for (Trick trick : mission.getCompletedTricks()) {
                    if (!trick.getPlays().isEmpty()) {
                        TrickPlay lead = trick.getPlays().get(0);
                        if (lead.getPlayerIndex() == playerIndex && lead.getCard().getColor() == color) {
                            leadForbidden = true;
                            break;
                        }
                    }
                }
                yield !leadForbidden;
            }
            case FIRST_AND_LAST -> {
                List<Trick> tricks = mission.getCompletedTricks();
                if (tricks.size() < 2) yield false;
                Trick first = tricks.get(0);
                Trick last = tricks.get(tricks.size() - 1);
                yield first.getWinnerIndex(first.getLeadSuit()) == playerIndex &&
                        last.getWinnerIndex(last.getLeadSuit()) == playerIndex;
            }
            default -> false;
        };
    }

    private int sumCards(Trick trick) {
        return trick.getPlays().stream().mapToInt(play -> play.getCard().getValue()).sum();
    }
}
