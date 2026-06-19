package game.thecrew.thread;

import game.thecrew.utils.ConfigurationUtils;
import game.thecrew.GameSession;
import game.thecrew.model.GameState;
import game.thecrew.model.PlayerInfo;

import game.thecrew.network.rmi.MissionServiceImpl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkThread extends Thread {

    private final int maxPlayers;
    private final AtomicInteger nextPlayerId = new AtomicInteger(0);
    private final ClientManager clientManager = new ClientManager();
    private final ActionHandler actionHandler;
    private static final Logger LOGGER = Logger.getLogger(NetworkThread.class.getName());

    public NetworkThread(int maxPlayers, MissionServiceImpl missionService) {
        this.maxPlayers = maxPlayers;
        this.actionHandler = new ActionHandler(null, clientManager, missionService, maxPlayers);
    }

    @Override
    public void run() {
        String portStr = ConfigurationUtils.getKey("server.port");
        int resolvedPort = Integer.parseInt(portStr);
        try (ServerSocket serverSocket = new ServerSocket(resolvedPort)) {
            while (nextPlayerId.get() < maxPlayers) {
                acceptAndInitializePlayer(serverSocket);
            }
            LOGGER.log(Level.INFO, "[DEBUG_LOG] Server reached max players ({0}). Sending start signal to {1} clients.", new Object[]{maxPlayers, clientManager.getClientCount()});

            clientManager.broadcastMessage("START_GAME");

            sleepAfterStartSignal();

            LOGGER.log(Level.INFO, "[DEBUG_LOG] Initializing Master GameSession with {0} players.", maxPlayers);
            GameSession session = new GameSession(maxPlayers);
            session.start();
            actionHandler.setSession(session);

            GameState state = session.getEngine().saveState();
            LOGGER.log(Level.FINE, "[DEBUG_LOG] Broadcasting GameState to {0} clients.", clientManager.getClientCount());
            clientManager.broadcastState(state);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server socket error", e);
        }
    }

    private void acceptAndInitializePlayer(ServerSocket serverSocket) {
        try {
            Socket clientSocket = serverSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            clientManager.addClient(out);

            int id = nextPlayerId.getAndIncrement();

            PlayerInfo assignedPlayer = new PlayerInfo("PLAYER_" + id, id, maxPlayers);

            out.writeObject(assignedPlayer);
            out.flush();

            clientManager.broadcastLobbyStatus(clientManager.getClientCount(), maxPlayers);

            LOGGER.log(Level.INFO, "[DEBUG_LOG] Assigned {0} to {1}", new Object[]{assignedPlayer.getName(), clientSocket.getInetAddress()});

            new ClientHandler(clientSocket, id, actionHandler).start();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error assigning player info: {0}", e.getMessage());
        }
    }

    private static void sleepAfterStartSignal() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
