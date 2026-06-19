package game.thecrew.thread;

import game.thecrew.model.GameState;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientManager {

    private static final Logger LOGGER = Logger.getLogger(ClientManager.class.getName());

    private final List<ObjectOutputStream> clientOutputStreams = Collections.synchronizedList(new ArrayList<>());

    public void addClient(ObjectOutputStream out) {
        clientOutputStreams.add(out);
    }

    public void broadcastLobbyStatus(int currentCount, int maxPlayers) {
        String message = "LOBBY_STATUS:" + currentCount + "/" + maxPlayers;
        for (ObjectOutputStream out : clientOutputStreams) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error broadcasting lobby status: {0}", e.getMessage());
            }
        }
    }

    public void broadcastMessage(Object message) {
        for (ObjectOutputStream out : clientOutputStreams) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error broadcasting message: {0}", e.getMessage());
            }
        }
    }

    public void broadcastState(GameState state) {
        for (ObjectOutputStream out : clientOutputStreams) {
            try {
                out.reset();
                out.writeObject(state);
                out.flush();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error broadcasting GameState: {0}", e.getMessage());
            }
        }
    }

    public int getClientCount() {
        return clientOutputStreams.size();
    }
}
