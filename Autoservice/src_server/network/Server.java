package network;

import session.SessionManager;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int port;
    private final SessionManager sessions = new SessionManager();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[SERVER] Listening on " + port);
            while (true) {
                Socket client = ss.accept();
                System.out.println("[SERVER] Client connected: " + client.getRemoteSocketAddress());
                new Thread(new ClientHandler(client, sessions)).start();
            }
        }
    }
}