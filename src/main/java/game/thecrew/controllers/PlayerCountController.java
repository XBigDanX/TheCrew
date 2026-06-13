package game.thecrew.controllers;

import game.thecrew.GameSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class PlayerCountController {

    @FXML
    private VBox root;

    @FXML
    private void onThreePlayers() {
        navigateToGameBoard(3);
    }

    @FXML
    private void onFourPlayers() {
        navigateToGameBoard(4);
    }

    @FXML
    private void onFivePlayers() {
        navigateToGameBoard(5);
    }

    private void navigateToGameBoard(int playerCount) {
        GameSession session = new GameSession(playerCount);
        session.start();
        GameController.setSession(session);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
            root.getScene().setRoot(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
