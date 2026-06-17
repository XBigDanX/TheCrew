package game.thecrew.network.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MissionLogServer {

    private static final Logger LOGGER = Logger.getLogger(MissionLogServer.class.getName());
    private static final int PORT = 1099;
    private static final String BIND_NAME = "MissionService";

    private MissionLogServer() {
    }

    public static MissionServiceImpl startServer() throws RemoteException {
        MissionServiceImpl service = new MissionServiceImpl();
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind(BIND_NAME, service);
        LOGGER.info("[RMI] MissionService bound on port " + PORT);
        return service;
    }

    public static void main(String[] args) {
        try {
            startServer();
            LOGGER.info("[RMI] MissionLogServer is running...");
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "[RMI] Failed to start server: {0}", e.getMessage());
        }
    }

}
