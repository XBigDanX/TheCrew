package game.thecrew.ui.managers;

import game.thecrew.GameApplication;
import game.thecrew.GameSession;
import game.thecrew.network.rmi.MissionClientCallback;
import game.thecrew.network.rmi.MissionService;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import static game.thecrew.utils.NetworkUtils.SERVER_ADDRESS;

public class RMIManager {

    private final TextArea missionLogArea;
    private GameSession session;
    private static final Logger LOGGER = Logger.getLogger(RMIManager.class.getName());

    public RMIManager(TextArea missionLogArea) {
        this.missionLogArea = missionLogArea;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public void setupRMI(MissionClientCallback callback) {
        try {
            UnicastRemoteObject.exportObject(callback, 0);
            Registry registry = LocateRegistry.getRegistry(SERVER_ADDRESS);
            MissionService service = (MissionService) registry.lookup("MissionService");
            String playerName = GameApplication.getPlayerInfo() != null
                ? "Player" + GameApplication.getPlayerInfo().getIndex()
                : "Unknown";
            service.registerClient(playerName, callback);
            if (session != null && session.getEngine() != null) {
                session.getEngine().setMissionService(service);
            }
            LOGGER.log(Level.INFO, "RMI: Registered with MissionService as {0}", playerName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "RMI: Failed to setup RMI", e);
        }
    }

    public void updateLog(int missionId, boolean success) {
        String result = success ? "Success" : "Failure";
        String line = "Mission " + missionId + ": " + result;
        Platform.runLater(() -> {
            LOGGER.log(Level.INFO, "RMI: {0}", line);
            if (missionLogArea != null) {
                missionLogArea.appendText(line + "\n");
            }
        });
    }
}
