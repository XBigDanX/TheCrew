package game.thecrew.model;

import java.util.List;

public class Mission {

    private final int id;
    private final int difficulty;
    private final List<ActiveMissionTask> tasks;

    public Mission(int id, int difficulty, List<ActiveMissionTask> tasks) {
        this.id = id;
        this.difficulty = difficulty;
        this.tasks = tasks;
    }

    public int getId() {
        return id;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public List<ActiveMissionTask> getTasks() {
        return tasks;
    }
}