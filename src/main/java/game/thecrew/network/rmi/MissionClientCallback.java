package game.thecrew.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MissionClientCallback extends Remote {

    void updateLog(int missionId, boolean success, String playerName) throws RemoteException;

}
