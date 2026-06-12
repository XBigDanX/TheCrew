package game.thecrew.model;

public class WinSpecificCardRule implements TaskRule {

    private final Card targetCard;

    public WinSpecificCardRule(Card targetCard) {
        this.targetCard = targetCard;
    }

    @Override
    public boolean isTrickBased() {
        return true;
    }

    @Override
    public boolean checkTrick(Mission mission, Trick trick, int winner) {
        return trick.getPlays().stream()
                .anyMatch(p -> p.getCard().equals(targetCard));
    }

    @Override
    public boolean checkMissionEnd(Mission mission, int playerIndex) {
        return true;
    }
}
