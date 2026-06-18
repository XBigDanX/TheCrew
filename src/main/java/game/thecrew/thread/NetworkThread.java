package game.thecrew.thread;

import game.thecrew.utils.ConfigurationUtils;
import game.thecrew.GameSession;
import game.thecrew.model.GameAction;
import game.thecrew.model.GameState;
import game.thecrew.model.PlayerInfo;
import game.thecrew.model.TokenPosition;
import game.thecrew.model.Card;
import game.thecrew.model.Mission;
import game.thecrew.model.MissionStatus;
import game.thecrew.model.Task;

import game.thecrew.network.rmi.MissionServiceImpl;

import java.rmi.RemoteException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkThread extends Thread {

    private final int maxPlayers;
    private final AtomicInteger nextPlayerId = new AtomicInteger(0);
    private final List<ObjectOutputStream> clientOutputStreams = Collections.synchronizedList(new ArrayList<>());
    private GameSession session;
    private MissionServiceImpl missionService;
    private static final Logger LOGGER = Logger.getLogger(NetworkThread.class.getName());

    public NetworkThread(int maxPlayers, MissionServiceImpl missionService) {
        this.maxPlayers = maxPlayers;
        this.missionService = missionService;
    }

    @Override
    public void run() {
        String portStr = ConfigurationUtils.getKey("server.port");
        int resolvedPort = Integer.parseInt(portStr);
        try (ServerSocket serverSocket = new ServerSocket(resolvedPort)) {
            while (nextPlayerId.get() < maxPlayers) {
                acceptAndInitializePlayer(serverSocket);
            }
            LOGGER.log(Level.INFO, "[DEBUG_LOG] Server reached max players ({0}). Sending start signal to {1} clients.", new Object[]{maxPlayers, clientOutputStreams.size()});

            sendStartSignalToAllClients();

            sleepAfterStartSignal();

            if (session == null) {
                LOGGER.log(Level.INFO, "[DEBUG_LOG] Initializing Master GameSession with {0} players.", maxPlayers);
                session = new GameSession(maxPlayers);
                session.start();
            }

            broadcastState();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server socket error", e);
        }
    }

    private void acceptAndInitializePlayer(ServerSocket serverSocket) {
        try {
            Socket clientSocket = serverSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            clientOutputStreams.add(out);

            int id = nextPlayerId.getAndIncrement();

            PlayerInfo assignedPlayer = new PlayerInfo("PLAYER_" + id, id, maxPlayers);

            out.writeObject(assignedPlayer);
            out.flush();

            broadcastLobbyStatus(clientOutputStreams.size());

            LOGGER.log(Level.INFO, "[DEBUG_LOG] Assigned {0} to {1}", new Object[]{assignedPlayer.getName(), clientSocket.getInetAddress()});

            new ClientHandler(clientSocket, id).start();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error assigning player info: {0}", e.getMessage());
        }
    }

    private void broadcastLobbyStatus(int currentJoinedCount) {
        for (ObjectOutputStream clientOut : clientOutputStreams) {
            try {
                clientOut.writeObject("LOBBY_STATUS:" + currentJoinedCount + "/" + maxPlayers);
                clientOut.flush();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error broadcasting lobby status: {0}", e.getMessage());
            }
        }
    }

    private void sendStartSignalToAllClients() {
        for (int i = 0; i < clientOutputStreams.size(); i++) {
            ObjectOutputStream out = clientOutputStreams.get(i);
            try {
                out.writeObject("START_GAME");
                out.flush();
                LOGGER.log(Level.FINE, "[DEBUG_LOG] Sent START_GAME to client {0}", i);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "[DEBUG_LOG] Error sending START_GAME to client {0}: {1}", new Object[]{i, e.getMessage()});
            }
        }
    }

    private void sendStateToClient(ObjectOutputStream out, GameState state, int clientIndex) {
        try {
            out.reset();
            out.writeObject(state);
            out.flush();
            LOGGER.log(Level.FINEST, "[DEBUG_LOG] Sent GameState to client {0}", clientIndex);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error broadcasting GameState to client {0}: {1}", new Object[]{clientIndex, e.getMessage()});
        }
    }

    private static void sleepAfterStartSignal() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void broadcastState() {
        GameState state = session.getEngine().saveState();
        LOGGER.log(Level.FINE, "[DEBUG_LOG] Broadcasting GameState to {0} clients.", clientOutputStreams.size());
        for (int i = 0; i < clientOutputStreams.size(); i++) {
            sendStateToClient(clientOutputStreams.get(i), state, i);
        }
    }

    private synchronized void handleAction(GameAction action) {
        LOGGER.log(Level.INFO, "[DEBUG_LOG] Processing action: {0}", action);
        boolean success;
        switch (action.getType()) {
            case PLAY_CARD -> {
                success = handlePlayCard(action);
                if (success) return;
            }
            case SELECT_TASK ->
                success = session.getEngine().selectTask(action.getPlayerIndex(), (Task) action.getPayload());
            case PASS_TASK_SELECTION ->
                success = session.getEngine().passTaskSelection(action.getPlayerIndex());
            case REQUEST_COMMUNICATION, SELECT_COMMUNICATION_CARD, DISMISS_COMMUNICATION ->
                success = handleCommunicationAction(action);
            case LOAD_GAME, NEXT_MISSION, RETRY_MISSION ->
                success = handleAdminAction(action);
            default -> success = false;
        }
        if (success) {
            broadcastState();
        } else {
            LOGGER.log(Level.INFO, "[DEBUG_LOG] Action failed: {0}", action);
        }
    }

    private boolean handlePlayCard(GameAction action) {
        if (action.getPlayerIndex() != session.getEngine().getPlayerManager().getCurrentPlayerIndex()) {
            LOGGER.log(Level.WARNING, "Out of turn play attempted");
            return false;
        }
        boolean success = session.getEngine().playCard(action.getPlayerIndex(), (Card) action.getPayload());
        if (success) {
            broadcastState();
            processPostPlayState();
        }
        return success;
    }

    private void processPostPlayState() {
        Mission mission = session.getEngine().getCurrentMission();
        if ((mission.getStatus() == MissionStatus.SUCCESS || mission.getStatus() == MissionStatus.FAILED) && missionService != null) {
            try {
                missionService.logMissionCompletion(mission.getId(), mission.getStatus() == MissionStatus.SUCCESS, "Server");
            } catch (RemoteException e) {
                LOGGER.log(Level.WARNING, "[RMI] Failed to log mission completion: {0}", e.getMessage());
            }
        }
        if (session.getEngine().getTrickManager().isComplete(maxPlayers)) {
            int winnerIndex = session.getEngine().getTrickManager().getWinner();
            for (ObjectOutputStream clientOut : clientOutputStreams) {
                try {
                    clientOut.writeObject("TRICK_WINNER:" + winnerIndex);
                    clientOut.flush();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error sending TRICK_WINNER: {0}", e.getMessage());
                }
            }
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                session.getEngine().resetTrick();
                broadcastState();
            }).start();
        }
    }

    private boolean handleCommunicationAction(GameAction action) {
        return switch (action.getType()) {
            case REQUEST_COMMUNICATION -> {
                session.getEngine().requestCommunication(action.getPlayerIndex());
                yield true;
            }
            case SELECT_COMMUNICATION_CARD -> {
                Object[] p = (Object[]) action.getPayload();
                boolean success = session.getEngine().selectCommunicationCard(action.getPlayerIndex(), (Card) p[0], (TokenPosition) p[1]);
                if (success) {
                    session.getEngine().applyPendingTokens();
                }
                yield success;
            }
            case DISMISS_COMMUNICATION -> {
                session.getEngine().removeActiveToken(action.getPlayerIndex());
                yield true;
            }
            default -> throw new IllegalArgumentException("Unexpected communication action: " + action.getType());
        };
    }

    private boolean handleAdminAction(GameAction action) {
        return switch (action.getType()) {
            case LOAD_GAME -> {
                session.getEngine().restoreState((GameState) action.getPayload());
                yield true;
            }
            case NEXT_MISSION -> {
                session.getEngine().advanceToNextMission();
                yield true;
            }
            case RETRY_MISSION -> {
                session.getEngine().restartCurrentMission();
                yield true;
            }
            default -> throw new IllegalArgumentException("Unexpected admin action: " + action.getType());
        };
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private final int playerIndex;

        public ClientHandler(Socket socket, int playerIndex) {
            this.socket = socket;
            this.playerIndex = playerIndex;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof GameAction) {
                        GameAction action = (GameAction) obj;
                        LOGGER.log(Level.INFO, "[DEBUG_LOG] Server received action: {0} from PLAYER_{1}", new Object[]{action.getType(), action.getPlayerIndex()});
                        handleAction(action);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.INFO, "Client PLAYER_{0} disconnected: {1}", new Object[]{playerIndex, socket.getInetAddress()});
            }
        }
    }
}
