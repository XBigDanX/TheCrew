package game.thecrew.controllers;

import game.thecrew.GameApplication;
import game.thecrew.model.PlayerInfo;
import game.thecrew.thread.NetworkThread;
import game.thecrew.utils.DocumentationUtils;
import game.thecrew.utils.NetworkUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class HomeScreenController {

    @FXML
    private TextField ipAddressField;

    @FXML
    private Button hostButton;

    @FXML
    private Button joinButton;

    @FXML
    private Button playButton;

    @FXML
    private void onHostGame() {
        GameApplication.playerInfo = new PlayerInfo("PLAYER_0", 0);

        NetworkThread nt = new NetworkThread(NetworkUtils.BASE_PORT);
        nt.setDaemon(true);
        nt.start();

        System.out.println("Hosting game on port 6000...");
    }

    @FXML
    private void onJoinGame() {
        String hostIp = ipAddressField.getText().isEmpty() ? "localhost" : ipAddressField.getText();

        new Thread(() -> {
            try (Socket socket = new Socket(hostIp, NetworkUtils.BASE_PORT);
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                PlayerInfo assignedInfo = (PlayerInfo) in.readObject();
                GameApplication.playerInfo = assignedInfo;

                System.out.println("Joined as: " + assignedInfo.getName());

                javafx.application.Platform.runLater(() -> {
                    NetworkThread nt = new NetworkThread(NetworkUtils.BASE_PORT + assignedInfo.getIndex());
                    nt.setDaemon(true);
                    nt.start();
                });

            } catch (Exception e) {
                System.err.println("Failed to join: " + e.getMessage());
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
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
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
