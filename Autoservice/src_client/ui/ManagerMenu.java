package ui;

import network.ClientConnection;
import util.Input;
import dto.Action;
import dto.Request;
import dto.Response;
import model.Appointment;
import model.Part;

import java.util.List;
import java.util.Map;

public class ManagerMenu {
    private final ClientConnection conn;
    private final Input in;
    private final String token;

    public ManagerMenu(ClientConnection conn, Input in, String token) {
        this.conn = conn;
        this.in = in;
        this.token = token;
    }

    public void run() throws Exception {
        while (true) {
            System.out.println("\n=== MANAGER MENU ===");
            System.out.println("1) Vezi cereri (pending neasignate)");
            System.out.println("2) Distribuie cerere la mecanic");
            System.out.println("3) Seteaza pret serviciu");
            System.out.println("4) Lista inventar piese");
            System.out.println("5) Adauga/editeaza piesa");
            System.out.println("6) Statistici");
            System.out.println("0) Logout");

            int c = in.intVal("Alege");
            switch (c) {
                case 1 -> listPending();
                case 2 -> assign();
                case 3 -> setPrice();
                case 4 -> listParts();
                case 5 -> upsertPart();
                case 6 -> stats();
                case 0 -> { logout(); return; }
                default -> System.out.println("Optiune invalida.");
            }
        }
    }

    private void listPending() throws Exception {
        Request req = new Request(Action.MANAGER_LIST_PENDING);
        req.setToken(token);
        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            List<Appointment> list = (List<Appointment>) r.getData().get("appointments");
            if (list == null || list.isEmpty()) {
                System.out.println("Nicio cerere pending neasignata.");
                return;
            }
            for (Appointment a : list) {
                System.out.println("ID=" + a.getId() + " | " + a.getServiceType() + " | " + a.getScheduledAt()
                        + " | cost=" + a.getEstimatedCost() + " | vehicul=" + a.getVehicleInfo());
            }
        }
    }

    private void assign() throws Exception {
        long appointmentId = in.longVal("Appointment ID");
        long mechanicId = in.longVal("Mechanic ID");

        Request req = new Request(Action.MANAGER_ASSIGN_TO_MECHANIC);
        req.setToken(token);
        req.getData().put("appointmentId", appointmentId);
        req.getData().put("mechanicId", mechanicId);

        Response r = conn.send(req);
        print(r);
    }

    private void setPrice() throws Exception {
        String type = in.str("ServiceType (REVISION/REPAIR)");
        double price = in.doubleVal("Pret (ex: 450.0)");

        Request req = new Request(Action.MANAGER_SET_SERVICE_PRICE);
        req.setToken(token);
        req.put("serviceType", type);
        req.getData().put("price", price);

        Response r = conn.send(req);
        print(r);
    }

    private void listParts() throws Exception {
        Request req = new Request(Action.MANAGER_LIST_PARTS);
        req.setToken(token);

        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            List<Part> list = (List<Part>) r.getData().get("parts");
            if (list == null || list.isEmpty()) {
                System.out.println("Inventar gol.");
                return;
            }
            for (Part p : list) {
                System.out.println("ID=" + p.getId() + " | " + p.getName() + " | stock=" + p.getStock() + " | price=" + p.getUnitPrice());
            }
        }
    }

    private void upsertPart() throws Exception {
        String idStr = in.str("ID piesa (gol pentru adaugare)");
        String name = in.str("Nume piesa");
        int stock = in.intVal("Stoc");
        double unit = in.doubleVal("Pret unit");

        Request req = new Request(Action.MANAGER_UPSERT_PART);
        req.setToken(token);

        if (!idStr.isBlank()) {
            try { req.getData().put("id", Long.parseLong(idStr.trim())); } catch (Exception ignored) {}
        } else {
            req.getData().put("id", null);
        }

        req.put("name", name);
        req.getData().put("stock", stock);
        req.getData().put("unitPrice", unit);

        Response r = conn.send(req);
        print(r);
    }

    private void stats() throws Exception {
        Request req = new Request(Action.MANAGER_GET_STATS);
        req.setToken(token);

        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) r.getData().get("stats");
            System.out.println("STATS=" + stats);
        }
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