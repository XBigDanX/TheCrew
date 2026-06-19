package game.thecrew.ui;

import game.thecrew.model.Task;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TaskView extends Pane {

    private final Task task;

    public TaskView(Task task) {
        this.task = task;

        setPrefSize(70, 50);
        setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");

        Text text = new Text(task.getDescription());
        text.setFont(Font.font(15));
        text.setWrappingWidth(60);
        text.setLayoutX(5);
        text.setLayoutY(12);

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
