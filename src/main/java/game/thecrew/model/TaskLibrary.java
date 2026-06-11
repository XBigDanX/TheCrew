package game.thecrew.model;

import java.util.List;

public class TaskLibrary {

    private TaskLibrary() {
    }

    public static List<Task> getAllTasks() {
        return List.of(
                new Task("Task 1", 1, 1, 1),
                new Task("Task 2", 2, 2, 2),
                new Task("Task 3", 1, 1, 1)
        );
    }
}
