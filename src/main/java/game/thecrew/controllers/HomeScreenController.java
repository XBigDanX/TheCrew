package game.thecrew.controllers;

import game.thecrew.utils.DocumentationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class HomeScreenController {

    @FXML
    private Button playButton;

    @FXML
    private void onPlayGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/thecrew/PlayerCount.fxml"));
            playButton.getScene().setRoot(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGenerateDocumentation() {
        DocumentationUtils.generateDocumentation();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documentation Generated");
        alert.setHeaderText(null);
        alert.setContentText("Documentation has been generated in documentation/missions.html");
        alert.showAndWait();
    }
}
