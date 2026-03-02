package network;

import dto.Request;
import dto.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public ClientConnection(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Response send(Request req) throws Exception {
        out.writeObject(req);
        out.flush();
        Object obj = in.readObject();
        return (Response) obj;
    }

    @Override
    public void close() throws Exception {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}