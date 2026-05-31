package game.thecrew.ui;

import game.thecrew.model.Task;
import javafx.scene.layout.StackPane;

public class TaskView extends StackPane {

    private final Task task;

    public TaskView(Task task) {
        this.task = task;
    }

}
