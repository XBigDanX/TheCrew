package game.thecrew.network.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionServiceImpl extends UnicastRemoteObject implements MissionService {

    private static final Logger LOGGER = Logger.getLogger(MissionServiceImpl.class.getName());
    private final ConcurrentHashMap<String, MissionClientCallback> clients = new ConcurrentHashMap<>();

    public MissionServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void registerClient(String name, MissionClientCallback callback) throws RemoteException {
        clients.put(name, callback);
        LOGGER.log(Level.INFO, "[RMI] Client registered: {0}", name);
    }

    @Override
    public void logMissionCompletion(int missionId, boolean success, String playerName) throws RemoteException {
        LOGGER.log(Level.INFO, "[RMI] Mission {0} completed by {1} with success={2}", new Object[]{missionId, playerName, success});
        broadcast(missionId, success, playerName);
    }

    public void broadcast(int missionId, boolean success, String playerName) {
        for (MissionClientCallback callback : clients.values()) {
            try {
                callback.updateLog(missionId, success, playerName);
            } catch (RemoteException e) {
                LOGGER.log(Level.WARNING, "[RMI] Failed to notify client: {0}", e.getMessage());
            }
        }
    }

}
