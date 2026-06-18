package game.thecrew.network;

import game.thecrew.GameSession;
import game.thecrew.controllers.GameController;
import game.thecrew.model.GameAction;
import game.thecrew.model.GameState;
import game.thecrew.model.TrickPlay;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameNetworkClient {

    private static final Logger LOGGER = Logger.getLogger(GameNetworkClient.class.getName());
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
                LOGGER.log(Level.SEVERE, "Failed to create output stream", e);
            }
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
                LOGGER.log(Level.INFO, "[DEBUG_LOG] Client lobby listener started.");
                while (true) {
                    Object obj = networkInputStream.readObject();
                    LOGGER.log(Level.FINE, "[DEBUG_LOG] Received object: {0} - {1}", new Object[]{obj == null ? "null" : obj.getClass().getName(), obj});
                    if (obj instanceof GameState) {
                        handleGameState(controller, (GameState) obj);
                    } else if (obj instanceof String) {
                        handleStringMessage(controller, (String) obj);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[DEBUG_LOG] Error in client lobby listener: {0}", e.getMessage());
                LOGGER.log(Level.FINE, "[DEBUG_LOG] Error in client lobby listener", e);
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    private void handleGameState(GameController controller, GameState receivedState) {
        Platform.runLater(() -> {
            initializeSessionIfNeeded(controller);

            List<TrickPlay> oldPlays = new ArrayList<>(controller.getSession().getEngine().getTrickManager().getCurrentTrick().getPlays());
            controller.getSession().getEngine().restoreState(receivedState);
            List<TrickPlay> newPlays = controller.getSession().getEngine().getTrickManager().getCurrentTrick().getPlays();

            boolean hadNewPlay = newPlays.size() > oldPlays.size();
            if (hadNewPlay) {
                animateNewPlays(controller, oldPlays, newPlays);
            }

            if (controller.getSession().getEngine().getTrickManager().isComplete(controller.getPlayerCount())) {
                handleTrickCompletion(controller, hadNewPlay);
            } else {
                updateStandardUI(controller, hadNewPlay);
            }
        });
    }

    private void initializeSessionIfNeeded(GameController controller) {
        if (controller.getSession() == null) {
            LOGGER.log(Level.INFO, "[DEBUG_LOG] Initializing session from received GameState for playerCount: {0}", controller.getPlayerCount());
            controller.setSession(new GameSession(controller.getPlayerCount()));
            controller.getSession().getEngine().createPlayers(controller.getPlayerCount());
            controller.initPlayerUIs();
            controller.setupPlayerViews();
            controller.lobbyOverlay.setManaged(false);
            controller.lobbyOverlay.setVisible(false);
        } else {
            controller.getSession().getEngine().createPlayers(controller.getPlayerCount());
        }
    }

    private void animateNewPlays(GameController controller, List<TrickPlay> oldPlays, List<TrickPlay> newPlays) {
        controller.refreshUI();
        for (int i = oldPlays.size(); i < newPlays.size(); i++) {
            TrickPlay play = newPlays.get(i);
            Pane sourcePane = controller.getPlayerUIs().get(play.getPlayerIndex()).getHand();
            controller.getTrickUIManager().animateCardToSlot(play.getCard(), play.getPlayerIndex(), sourcePane);
        }
    }

    private void handleTrickCompletion(GameController controller, boolean hadNewPlay) {
        if (!hadNewPlay) {
            controller.renderCurrentTrick();
        }
        controller.renderAllHands();
        controller.updateInfoLabels();
        PauseTransition delay = new PauseTransition(Duration.millis(hadNewPlay ? 800 : 100));
        delay.setOnFinished(e -> {
            int winnerIndex = controller.getSession().getEngine().getTrickManager().getWinner();
            controller.getTrickUIManager().animateTrickEnd(winnerIndex, () -> {
                controller.getSession().getEngine().resetTrick();
                controller.refreshUI();
            });
        });
        delay.play();
    }

    private void updateStandardUI(GameController controller, boolean hadNewPlay) {
        controller.renderAllHands();
        controller.updateInfoLabels();
        if (!hadNewPlay) {
            controller.refreshUI();
        }
    }

    private void handleStringMessage(GameController controller, String msg) {
        if ("START_GAME".equals(msg)) {
            handleStartGame(controller);
        } else if (msg.startsWith("LOBBY_STATUS:")) {
            String status = msg.substring("LOBBY_STATUS:".length());
            Platform.runLater(() -> controller.lobbyStatusLabel.setText("Waiting for Players... (" + status + ")"));
        } else if (msg.startsWith("TRICK_WINNER:")) {
            String idStr = msg.substring("TRICK_WINNER:".length());
            int winnerId = Integer.parseInt(idStr);
            Platform.runLater(() -> controller.showTrickWinner(winnerId));
        }
    }

    private void handleStartGame(GameController controller) {
        LOGGER.log(Level.INFO, "[DEBUG_LOG] START_GAME signal received.");
        Platform.runLater(() -> {
            if (controller.getSession() == null) {
                LOGGER.log(Level.INFO, "[DEBUG_LOG] START_GAME received. Initializing session locally for playerCount: {0}", controller.getPlayerCount());
                controller.setSession(new GameSession(controller.getPlayerCount()));
                controller.getSession().getEngine().createPlayers(controller.getPlayerCount());
                controller.initPlayerUIs();
                controller.setupPlayerViews();
            }
            controller.lobbyOverlay.setManaged(false);
            controller.lobbyOverlay.setVisible(false);
            LOGGER.log(Level.INFO, "[DEBUG_LOG] Lobby overlay hidden.");
        });
    }

    public void sendAction(GameAction action) {
        if (networkOutputStream != null) {
            try {
                networkOutputStream.reset();
                networkOutputStream.writeObject(action);
                networkOutputStream.flush();
                LOGGER.log(Level.FINE, "[DEBUG_LOG] Sent action: {0}", action);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to send action", e);
            }
        }
    }
}
