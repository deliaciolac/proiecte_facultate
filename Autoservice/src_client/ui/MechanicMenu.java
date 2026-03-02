package ui;

import network.ClientConnection;
import util.Input;
import dto.Action;
import dto.Request;
import dto.Response;
import model.Appointment;
import model.UsedPart;

import java.util.ArrayList;
import java.util.List;

public class MechanicMenu {
    private final ClientConnection conn;
    private final Input in;
    private final String token;

    public MechanicMenu(ClientConnection conn, Input in, String token) {
        this.conn = conn;
        this.in = in;
        this.token = token;
    }

    public void run() throws Exception {
        while (true) {
            System.out.println("\n=== MECHANIC MENU ===");
            System.out.println("1) Lista lucrari asignate");
            System.out.println("2) Update status (PENDING/IN_PROGRESS/COMPLETED)");
            System.out.println("3) Adauga piese + durata + note");
            System.out.println("0) Logout");

            int c = in.intVal("Alege");
            switch (c) {
                case 1 -> listAssigned();
                case 2 -> updateStatus();
                case 3 -> addPartsDuration();
                case 0 -> { logout(); return; }
                default -> System.out.println("Optiune invalida.");
            }
        }
    }

    private void listAssigned() throws Exception {
        Request req = new Request(Action.MECHANIC_LIST_ASSIGNED);
        req.setToken(token);
        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            List<Appointment> list = (List<Appointment>) r.getData().get("appointments");
            if (list == null || list.isEmpty()) {
                System.out.println("Nicio lucrare asignata.");
                return;
            }
            for (Appointment a : list) {
                System.out.println("ID=" + a.getId()
                        + " | " + a.getServiceType()
                        + " | " + a.getScheduledAt()
                        + " | status=" + a.getStatus()
                        + " | vehicul=" + a.getVehicleInfo()
                        + " | clientNotes=" + a.getClientNotes());
            }
        }
    }

    private void updateStatus() throws Exception {
        long id = in.longVal("Appointment ID");
        String st = in.str("Status (PENDING/IN_PROGRESS/COMPLETED)");

        Request req = new Request(Action.MECHANIC_UPDATE_STATUS);
        req.setToken(token);
        req.getData().put("appointmentId", id);
        req.put("status", st);

        Response r = conn.send(req);
        print(r);
    }

    private void addPartsDuration() throws Exception {
        long id = in.longVal("Appointment ID");
        int duration = in.intVal("Durata (minute)");
        String notes = in.str("Note mecanic");

        List<UsedPart> parts = new ArrayList<>();
        while (true) {
            String more = in.str("Adaugi piesa? (da/nu)");
            if (!more.equalsIgnoreCase("da")) break;

            String partIdStr = in.str("Part ID (gol daca nu stii)"); // optional
            String partName = in.str("Nume piesa");
            int qty = in.intVal("Cantitate");

            UsedPart up = new UsedPart();
            up.setAppointmentId(id);
            up.setPartName(partName);
            up.setQuantity(qty);

            if (!partIdStr.isBlank()) {
                try { up.setPartId(Long.parseLong(partIdStr.trim())); } catch (Exception ignored) {}
            }

            parts.add(up);
        }

        Request req = new Request(Action.MECHANIC_ADD_PARTS_AND_DURATION);
        req.setToken(token);
        req.getData().put("appointmentId", id);
        req.getData().put("durationMinutes", duration);
        req.put("mechanicNotes", notes);
        req.getData().put("partsUsed", parts);

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