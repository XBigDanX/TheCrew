package game.thecrew.model;

import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final int id;
    private final int difficulty;
    private final List<Task> tasks = new ArrayList<>();

    public Mission(int id, int difficulty) {
        this.id = id;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}