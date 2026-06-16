package game.thecrew.controllers;

import game.thecrew.network.rmi.MissionLogServer;
import game.thecrew.network.rmi.MissionServiceImpl;
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
        MissionServiceImpl missionService = null;
        try {
            missionService = MissionLogServer.startServer();
        } catch (Exception e) {
            System.err.println("[RMI] Failed to start MissionLogServer: " + e.getMessage());
            e.printStackTrace();
        }

        NetworkThread nt = new NetworkThread(NetworkUtils.BASE_PORT, playerCount, missionService);
        nt.setDaemon(true);
        nt.start();

        HomeScreenController.joinGame("localhost", twoPlayersButton);
    }
}
