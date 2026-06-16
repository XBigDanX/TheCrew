package game.thecrew.network.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class MissionLogServer {

    private static final int PORT = 1099;
    private static final String BIND_NAME = "MissionService";

    private MissionLogServer() {
    }

    public static MissionServiceImpl startServer() throws RemoteException {
        MissionServiceImpl service = new MissionServiceImpl();
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind(BIND_NAME, service);
        System.out.println("[RMI] MissionService bound on port " + PORT);
        return service;
    }

    public static void main(String[] args) {
        try {
            startServer();
            System.out.println("[RMI] MissionLogServer is running...");
        } catch (RemoteException e) {
            System.err.println("[RMI] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
