package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.model.PlayerInfo;
import game.thecrew.thread.NetworkThread;
import game.thecrew.utils.NetworkUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class PlayerCountController {

    @FXML
    private VBox root;

    @FXML
    private Button twoPlayersButton;

    @FXML
    private void onTwoPlayers() {
        navigateToGameBoard(2);
    }

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
        NetworkThread nt = new NetworkThread(NetworkUtils.BASE_PORT, playerCount);
        nt.setDaemon(true);
        nt.start();

        HomeScreenController.joinGame("localhost", twoPlayersButton);
    }
}
