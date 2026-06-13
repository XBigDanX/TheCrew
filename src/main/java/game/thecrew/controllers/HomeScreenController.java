package game.thecrew.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
}
