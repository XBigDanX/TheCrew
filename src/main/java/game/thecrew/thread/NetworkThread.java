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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkThread extends Thread {

    private final int port;
    private final int maxPlayers;
    private final AtomicInteger nextPlayerId = new AtomicInteger(0);
    private final List<ObjectOutputStream> clientOutputStreams = Collections.synchronizedList(new ArrayList<>());
    private GameSession session;
    private MissionServiceImpl missionService;

    public NetworkThread(int port, int maxPlayers, MissionServiceImpl missionService) {
        this.port = port;
        this.maxPlayers = maxPlayers;
        this.missionService = missionService;
    }

    @Override
    public void run() {
        String portStr = ConfigurationUtils.getKey("server.port");
        int resolvedPort = Integer.parseInt(portStr);
        try (ServerSocket serverSocket = new ServerSocket(resolvedPort)) {
            while (nextPlayerId.get() < maxPlayers) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    clientOutputStreams.add(out);

                    int id = nextPlayerId.getAndIncrement();

                    PlayerInfo assignedPlayer = new PlayerInfo("PLAYER_" + id, id, maxPlayers);

                    out.writeObject(assignedPlayer);
                    out.flush();

                    // Broadcast lobby status to all currently connected clients
                    int currentJoinedCount = clientOutputStreams.size();
                    for (ObjectOutputStream clientOut : clientOutputStreams) {
                        try {
                            clientOut.writeObject("LOBBY_STATUS:" + currentJoinedCount + "/" + maxPlayers);
                            clientOut.flush();
                        } catch (IOException e) {
                            System.err.println("Error broadcasting lobby status: " + e.getMessage());
                        }
                    }

                    System.out.println("[DEBUG_LOG] Assigned " + assignedPlayer.getName() + " to " + clientSocket.getInetAddress());

                    // Start a handler for this client to listen for actions
                    new ClientHandler(clientSocket, id).start();

                } catch (Exception e) {
                    System.err.println("Error assigning player info: " + e.getMessage());
                }
            }
            System.out.println("[DEBUG_LOG] Server reached max players (" + maxPlayers + "). Sending start signal to " + clientOutputStreams.size() + " clients.");

            for (int i = 0; i < clientOutputStreams.size(); i++) {
                ObjectOutputStream out = clientOutputStreams.get(i);
                try {
                    out.writeObject("START_GAME");
                    out.flush();
                    System.out.println("[DEBUG_LOG] Sent START_GAME to client " + i);
                } catch (IOException e) {
                    System.err.println("[DEBUG_LOG] Error sending START_GAME to client " + i + ": " + e.getMessage());
                }
            }

            // Small delay to ensure clients have processed START_GAME before GameState arrives
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (session == null) {
                System.out.println("[DEBUG_LOG] Initializing Master GameSession with " + maxPlayers + " players.");
                session = new GameSession(maxPlayers);
                session.start();
            }

            broadcastState();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void broadcastState() {
        GameState state = session.getEngine().saveState();
        System.out.println("[DEBUG_LOG] Broadcasting GameState to " + clientOutputStreams.size() + " clients.");
        for (int i = 0; i < clientOutputStreams.size(); i++) {
            ObjectOutputStream out = clientOutputStreams.get(i);
            try {
                out.reset();
                out.writeObject(state);
                out.flush();
                System.out.println("[DEBUG_LOG] Sent GameState to client " + i);
            } catch (IOException e) {
                System.err.println("Error broadcasting GameState to client " + i + ": " + e.getMessage());
            }
        }
    }

    private synchronized void handleAction(GameAction action) {
        System.out.println("[DEBUG_LOG] Processing action: " + action);
        boolean success = false;
        switch (action.getType()) {
            case PLAY_CARD:
                if (action.getPlayerIndex() != session.getEngine().getPlayerManager().getCurrentPlayerIndex()) {
                    System.out.println("Out of turn play attempted");
                    success = false;
                    break;
                }
                success = session.getEngine().playCard(action.getPlayerIndex(), (Card) action.getPayload());
                if (success) {
                    broadcastState();
                    Mission mission = session.getEngine().getCurrentMission();
                    if (mission.getStatus() == MissionStatus.SUCCESS || mission.getStatus() == MissionStatus.FAILED) {
                        if (missionService != null) {
                            try {
                                missionService.logMissionCompletion(mission.getId(), mission.getStatus() == MissionStatus.SUCCESS, "Server");
                            } catch (RemoteException e) {
                                System.err.println("[RMI] Failed to log mission completion: " + e.getMessage());
                            }
                        }
                    }
                    if (session.getEngine().getTrickManager().isComplete(maxPlayers)) {
                        int winnerIndex = session.getEngine().getTrickManager().getWinner();
                        for (ObjectOutputStream clientOut : clientOutputStreams) {
                            try {
                                clientOut.writeObject("TRICK_WINNER:" + winnerIndex);
                                clientOut.flush();
                            } catch (IOException e) {
                                System.err.println("Error sending TRICK_WINNER: " + e.getMessage());
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
                    return;
                }
                break;
            case SELECT_TASK:
                success = session.getEngine().selectTask(action.getPlayerIndex(), (Task) action.getPayload());
                break;
            case PASS_TASK_SELECTION:
                success = session.getEngine().passTaskSelection(action.getPlayerIndex());
                break;
            case REQUEST_COMMUNICATION:
                session.getEngine().requestCommunication(action.getPlayerIndex());
                success = true;
                break;
            case SELECT_COMMUNICATION_CARD:
                Object[] p = (Object[]) action.getPayload();
                success = session.getEngine().selectCommunicationCard(action.getPlayerIndex(), (Card) p[0], (TokenPosition) p[1]);
                if (success) {
                    session.getEngine().applyPendingTokens();
                }
                break;
            case DISMISS_COMMUNICATION:
                session.getEngine().removeActiveToken(action.getPlayerIndex());
                success = true;
                break;
            case LOAD_GAME:
                session.getEngine().restoreState((GameState) action.getPayload());
                success = true;
                break;
            case NEXT_MISSION:
                session.getEngine().advanceToNextMission();
                success = true;
                break;
            case RETRY_MISSION:
                session.getEngine().restartCurrentMission();
                success = true;
                break;
        }

        if (success) {
            broadcastState();
        } else {
            System.out.println("[DEBUG_LOG] Action failed: " + action);
        }
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
                        System.out.println("[DEBUG_LOG] Server received action: " + action.getType() + " from PLAYER_" + action.getPlayerIndex());
                        handleAction(action);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client PLAYER_" + playerIndex + " disconnected: " + socket.getInetAddress());
            }
        }
    }
}
