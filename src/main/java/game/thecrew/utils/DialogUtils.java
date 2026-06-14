package game.thecrew.utils;

import javafx.scene.control.Alert;

public class DialogUtils {
    private DialogUtils() {}

    public static void showDialog(String title, String content, String header, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
