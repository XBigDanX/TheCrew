package game.thecrew.model;

public class Task {

    private final String description;
    private final int value23;
    private final int value4;
    private final int value5;
    private final TaskRule rule;

    public Task(String description, int value23, int value4, int value5, TaskRule rule) {
        this.description = description;
        this.value23 = value23;
        this.value4 = value4;
        this.value5 = value5;
        this.rule = rule;
    }

    public String getDescription() {
        return description;
    }

    public TaskRule getRule() {
        return rule;
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