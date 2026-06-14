package game.thecrew.thread;

import game.thecrew.model.PlayerInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkThread extends Thread {

    private final int port;
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);

    public NetworkThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    int id = nextPlayerId.getAndIncrement();
                    PlayerInfo assignedPlayer = new PlayerInfo("PLAYER_" + id, id);

                    out.writeObject(assignedPlayer);
                    out.flush();

                    System.out.println("Assigned " + assignedPlayer.getName() + " to " + clientSocket.getInetAddress());

                } catch (Exception e) {
                    System.err.println("Error assigning player info: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
