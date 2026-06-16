package game.thecrew.network.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class MissionServiceImpl extends UnicastRemoteObject implements MissionService {

    private final ConcurrentHashMap<String, MissionClientCallback> clients = new ConcurrentHashMap<>();

    public MissionServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void registerClient(String name, MissionClientCallback callback) throws RemoteException {
        clients.put(name, callback);
        System.out.println("[RMI] Client registered: " + name);
    }

    @Override
    public void logMissionCompletion(int missionId, boolean success, String playerName) throws RemoteException {
        System.out.println("[RMI] Mission " + missionId + " completed by " + playerName + " with success=" + success);
        broadcast(missionId, success, playerName);
    }

    public void broadcast(int missionId, boolean success, String playerName) {
        for (MissionClientCallback callback : clients.values()) {
            try {
                callback.updateLog(missionId, success, playerName);
            } catch (RemoteException e) {
                System.err.println("[RMI] Failed to notify client: " + e.getMessage());
            }
        }
    }

}
