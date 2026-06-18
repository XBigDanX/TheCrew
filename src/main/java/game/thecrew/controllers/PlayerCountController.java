package game.thecrew.controllers;

import game.thecrew.network.rmi.MissionLogServer;
import game.thecrew.network.rmi.MissionServiceImpl;
import game.thecrew.thread.NetworkThread;
import game.thecrew.utils.NetworkUtils;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class PlayerCountController {

    private static final Logger LOGGER = Logger.getLogger(PlayerCountController.class.getName());

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
            LOGGER.log(Level.SEVERE, "[RMI] Failed to start MissionLogServer", e);
        }

        NetworkThread nt = new NetworkThread(playerCount, missionService);
        nt.setDaemon(true);
        nt.start();

        HomeScreenController.joinGame("localhost", twoPlayersButton);
    }
}
