package game.thecrew.ui;

import game.thecrew.model.Task;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class TaskView extends Pane {

    private final Task task;

    public TaskView(Task task) {
        this.task = task;

        setPrefSize(80, 100);
        setStyle("-fx-background-color: white;");

        Text text = new Text(task.getDescription());
        text.setLayoutX(10);
        text.setLayoutY(50);

        getChildren().add(text);
    }

    public Task getTask() {
        return task;
    }

    public void setCompleted(boolean completed) {
        if (completed) {
            setStyle("-fx-background-color: lightgreen;");
        } else {
            setStyle("-fx-background-color: white;");
        }
    }
}
