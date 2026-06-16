package game.thecrew.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MissionService extends Remote {

    void registerClient(String name, MissionClientCallback callback) throws RemoteException;

    void logMissionCompletion(int missionId, boolean success, String playerName) throws RemoteException;

}
