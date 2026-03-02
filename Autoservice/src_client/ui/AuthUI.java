package ui;

import network.ClientConnection;
import util.Input;
import dto.Action;
import dto.Request;
import dto.Response;

public class AuthUI {
    private final ClientConnection conn;
    private final Input in;

    public AuthUI(ClientConnection conn, Input in) {
        this.conn = conn;
        this.in = in;
    }

    public Response login() throws Exception {
        String u = in.str("Username");
        String p = in.str("Password");

        Request req = new Request(Action.LOGIN)
                .put("username", u)
                .put("password", p);

        return conn.send(req);
    }

    public Response registerClient() throws Exception {
        String u = in.str("Username");
        String p = in.str("Password (min 4)");

        Request req = new Request(Action.REGISTER_CLIENT)
                .put("username", u)
                .put("password", p);

        return conn.send(req);
    }

    public Response registerMechanic() throws Exception {
        String u = in.str("Username");
        String p = in.str("Password (min 4)");

        Request req = new Request(Action.REGISTER_MECHANIC)
                .put("username", u)
                .put("password", p);

        return conn.send(req);
    }
}