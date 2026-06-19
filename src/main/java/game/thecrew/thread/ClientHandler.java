package game.thecrew.thread;

import game.thecrew.model.GameAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final int playerIndex;
    private final ActionHandler actionHandler;

    public ClientHandler(Socket socket, int playerIndex, ActionHandler actionHandler) {
        this.socket = socket;
        this.playerIndex = playerIndex;
        this.actionHandler = actionHandler;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof GameAction) {
                    GameAction action = (GameAction) obj;
                    LOGGER.log(Level.INFO, "[DEBUG_LOG] Server received action: {0} from PLAYER_{1}", new Object[]{action.getType(), action.getPlayerIndex()});
                    actionHandler.handleAction(action);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.INFO, "Client PLAYER_{0} disconnected: {1}", new Object[]{playerIndex, socket.getInetAddress()});
        }
    }
}
