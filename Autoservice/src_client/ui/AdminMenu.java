package ui;

import network.ClientConnection;
import util.Input;
import dto.Action;
import dto.Request;
import dto.Response;
import model.User;

import java.util.List;

public class AdminMenu {
    private final ClientConnection conn;
    private final Input in;
    private final String token;

    public AdminMenu(ClientConnection conn, Input in, String token) {
        this.conn = conn;
        this.in = in;
        this.token = token;
    }

    public void run() throws Exception {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1) Lista utilizatori");
            System.out.println("2) Adauga mecanic");
            System.out.println("3) Editeaza mecanic (username)");
            System.out.println("4) Sterge mecanic");
            System.out.println("5) Promoveaza client -> mecanic");
            System.out.println("0) Logout");

            int c = in.intVal("Alege");
            switch (c) {
                case 1 -> listUsers();
                case 2 -> addMechanic();
                case 3 -> editMechanic();
                case 4 -> removeMechanic();
                case 5 -> promote();
                case 0 -> { logout(); return; }
                default -> System.out.println("Optiune invalida.");
            }
        }
    }

    private void listUsers() throws Exception {
        Request req = new Request(Action.ADMIN_LIST_USERS);
        req.setToken(token);
        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            List<User> list = (List<User>) r.getData().get("users");
            if (list == null || list.isEmpty()) {
                System.out.println("Niciun user.");
                return;
            }
            for (User u : list) {
                System.out.println("ID=" + u.getId() + " | " + u.getUsername() + " | role=" + u.getRole());
            }
        }
    }

    private void addMechanic() throws Exception {
        String u = in.str("Username mecanic");
        String p = in.str("Parola");
        Request req = new Request(Action.ADMIN_ADD_MECHANIC);
        req.setToken(token);
        req.put("username", u);
        req.put("password", p);
        Response r = conn.send(req);
        print(r);
    }

    private void editMechanic() throws Exception {
        long id = in.longVal("Mechanic ID");
        String newU = in.str("Noul username");
        Request req = new Request(Action.ADMIN_EDIT_MECHANIC);
        req.setToken(token);
        req.getData().put("mechanicId", id);
        req.put("newUsername", newU);
        Response r = conn.send(req);
        print(r);
    }

    private void removeMechanic() throws Exception {
        long id = in.longVal("Mechanic ID");
        Request req = new Request(Action.ADMIN_REMOVE_MECHANIC);
        req.setToken(token);
        req.getData().put("mechanicId", id);
        Response r = conn.send(req);
        print(r);
    }

    private void promote() throws Exception {
        long id = in.longVal("Client ID");
        Request req = new Request(Action.ADMIN_PROMOTE_CLIENT_TO_MECHANIC);
        req.setToken(token);
        req.getData().put("clientId", id);
        Response r = conn.send(req);
        print(r);
    }

    private void logout() throws Exception {
        Request req = new Request(Action.LOGOUT);
        req.setToken(token);
        Response r = conn.send(req);
        print(r);
    }

    private static void print(Response r) {
        System.out.println((r.isOk() ? "[OK] " : "[ERR] ") + r.getMessage());
    }
}