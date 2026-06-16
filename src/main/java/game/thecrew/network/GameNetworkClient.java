package game.thecrew.network;

import game.thecrew.GameSession;
import game.thecrew.controllers.GameController;
import game.thecrew.model.GameAction;
import game.thecrew.model.GameState;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameNetworkClient {

    private static Socket pendingSocket;
    private static ObjectInputStream pendingInputStream;

    private Socket networkSocket;
    private ObjectInputStream networkInputStream;
    private ObjectOutputStream networkOutputStream;

    public static void setNetworkConnection(Socket socket, ObjectInputStream inputStream) {
        pendingSocket = socket;
        pendingInputStream = inputStream;
    }

    public GameNetworkClient() {
        if (pendingSocket != null && pendingInputStream != null) {
            this.networkSocket = pendingSocket;
            this.networkInputStream = pendingInputStream;
            try {
                this.networkOutputStream = new ObjectOutputStream(networkSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            pendingSocket = null;
            pendingInputStream = null;
        }
    }

    public boolean isConnected() {
        return networkInputStream != null;
    }

    public boolean isOutputStreamReady() {
        return networkOutputStream != null;
    }

    public void listen(GameController controller) {
        controller.lobbyOverlay.setManaged(true);
        controller.lobbyOverlay.setVisible(true);

        Thread listener = new Thread(() -> {
            try {
                System.out.println("[DEBUG_LOG] Client lobby listener started.");
                while (true) {
                    Object obj = networkInputStream.readObject();
                    System.out.println("[DEBUG_LOG] Received object: " + (obj == null ? "null" : obj.getClass().getName()) + " - " + obj);
                    if (obj instanceof GameState) {
                        GameState receivedState = (GameState) obj;
                        Platform.runLater(() -> {
                            if (controller.session == null) {
                                System.out.println("[DEBUG_LOG] Initializing session from received GameState for playerCount: " + controller.playerCount);
                                controller.session = new GameSession(controller.playerCount);
                                controller.session.getEngine().createPlayers(controller.playerCount);
                                controller.initPlayerUIs();
                                controller.setupPlayerViews();
                                controller.lobbyOverlay.setManaged(false);
                                controller.lobbyOverlay.setVisible(false);
                            } else {
                                controller.session.getEngine().createPlayers(controller.playerCount);
                            }
                            System.out.println("[DEBUG_LOG] Restoring GameState.");
                            controller.session.getEngine().restoreState(receivedState);
                            if (controller.session.getEngine().getTrickManager().isComplete(controller.playerCount)) {
                                controller.renderCurrentTrick();
                                controller.renderAllHands();
                                controller.updateInfoLabels();
                                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                                delay.setOnFinished(e -> {
                                    controller.session.getEngine().resetTrick();
                                    controller.refreshUI();
                                });
                                delay.play();
                            } else {
                                controller.refreshUI();
                            }
                        });
                    } else if (obj instanceof String) {
                        String msg = (String) obj;
                        if ("START_GAME".equals(msg)) {
                            System.out.println("[DEBUG_LOG] START_GAME signal received.");
                            Platform.runLater(() -> {
                                if (controller.session == null) {
                                    System.out.println("[DEBUG_LOG] START_GAME received. Initializing session locally for playerCount: " + controller.playerCount);
                                    controller.session = new GameSession(controller.playerCount);
                                    controller.session.getEngine().createPlayers(controller.playerCount);
                                    controller.initPlayerUIs();
                                    controller.setupPlayerViews();
                                }
                                controller.lobbyOverlay.setManaged(false);
                                controller.lobbyOverlay.setVisible(false);
                                System.out.println("[DEBUG_LOG] Lobby overlay hidden.");
                            });
                        } else if (msg.startsWith("LOBBY_STATUS:")) {
                            String status = msg.substring("LOBBY_STATUS:".length());
                            Platform.runLater(() -> {
                                controller.lobbyStatusLabel.setText("Waiting for Players... (" + status + ")");
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[DEBUG_LOG] Error in client lobby listener: " + e.getMessage());
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void sendAction(GameAction action) {
        if (networkOutputStream != null) {
            try {
                networkOutputStream.reset();
                networkOutputStream.writeObject(action);
                networkOutputStream.flush();
                System.out.println("[DEBUG_LOG] Sent action: " + action);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
