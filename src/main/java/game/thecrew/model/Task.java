package game.thecrew.model;

public class Task {

    private final String description;
    private final int value23;
    private final int value4;
    private final int value5;

    public Task(String description, int value23, int value4, int value5) {
        this.description = description;
        this.value23 = value23;
        this.value4 = value4;
        this.value5 = value5;
    }

    public String getDescription() {
        return description;
    }

    public int getValue(int playerCount) {
        return switch (playerCount) {
            case 2, 3 -> value23;
            case 4 -> value4;
            case 5 -> value5;
            default -> throw new IllegalArgumentException("Unsupported player count: " + playerCount);
        };
    }
}