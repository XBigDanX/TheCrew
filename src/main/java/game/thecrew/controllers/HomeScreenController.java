package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.model.PlayerInfo;
import game.thecrew.network.GameNetworkClient;
import game.thecrew.utils.ConfigurationUtils;
import game.thecrew.utils.DocumentationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static game.thecrew.utils.NetworkUtils.SERVER_ADDRESS;

public class HomeScreenController {

    private static final Logger LOGGER = Logger.getLogger(HomeScreenController.class.getName());

    @FXML
    private TextField ipAddressField;

    @FXML
    private Button hostButton;

    @FXML
    private Button joinButton;

    @FXML
    private Button playButton;

    public static final String ERROR_MSG = "Error";
    public static final String NAVIGATION_ERROR_MSG = "Navigation Error";

    @FXML
    private void onHostGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/thecrew/PlayerCount.fxml"));
            hostButton.getScene().setRoot(loader.load());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_MSG);
            alert.setHeaderText(NAVIGATION_ERROR_MSG);
            alert.setContentText("Could not load player count selection: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onJoinGame() {
        String hostIp = ipAddressField.getText().isEmpty() ? SERVER_ADDRESS : ipAddressField.getText();
        joinGame(hostIp, joinButton);
    }

    public static void joinGame(String hostIp, Button sourceButton) {
        new Thread(() -> {
            try {
                String portStr = ConfigurationUtils.getKey("server.port");
                int port = Integer.parseInt(portStr);
                Socket socket = new Socket(hostIp, port);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                PlayerInfo assignedInfo = (PlayerInfo) in.readObject();
                GameApplication.setPlayerInfo(assignedInfo);

                LOGGER.log(Level.INFO, "Joined as: {0}", assignedInfo.getName());

                javafx.application.Platform.runLater(() -> {
                    GameNetworkClient.setNetworkConnection(socket, in);
                    try {
                        FXMLLoader loader = new FXMLLoader(HomeScreenController.class.getResource("/game/thecrew/GameBoard.fxml"));
                        sourceButton.getScene().setRoot(loader.load());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Could not load the game board", e);
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(ERROR_MSG);
                        alert.setHeaderText(NAVIGATION_ERROR_MSG);
                        alert.setContentText("Could not load the game board: " + e.getMessage());
                        alert.showAndWait();
                    }
                });

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to join: {0}", e.getMessage());
            }
        }).start();
    }

    @FXML
    private void onPlayGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/thecrew/PlayerCount.fxml"));
            playButton.getScene().setRoot(loader.load());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_MSG);
            alert.setHeaderText(NAVIGATION_ERROR_MSG);
            alert.setContentText("Could not load the game board: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onGenerateDocumentation() {
        DocumentationUtils.generateDocumentation();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Documentation Generated");
        alert.setHeaderText(null);
        alert.setContentText("Documentation has been generated in documentation/docs.html");
        alert.showAndWait();
    }
}
