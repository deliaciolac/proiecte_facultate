package ui;

import network.ClientConnection;
import util.Input;
import dto.Action;
import dto.Request;
import dto.Response;
import model.Appointment;

import java.util.List;

public class ClientMenu {
    private final ClientConnection conn;
    private final Input in;
    private final String token;

    public ClientMenu(ClientConnection conn, Input in, String token) {
        this.conn = conn;
        this.in = in;
        this.token = token;
    }

    public void run() throws Exception {
        while (true) {
            System.out.println("\n=== CLIENT MENU ===");
            System.out.println("1) Programeaza revizie/reparatie");
            System.out.println("2) Vezi programarile mele");
            System.out.println("3) Adauga observatii la o programare");
            System.out.println("4) Notificari finalizare");
            System.out.println("5) Feedback (dupa COMPLETED)");
            System.out.println("0) Logout");

            int c = in.intVal("Alege");

            switch (c) {
                case 1 -> createAppointment();
                case 2 -> listAppointments();
                case 3 -> addNotes();
                case 4 -> notifications();
                case 5 -> feedback();
                case 0 -> { logout(); return; }
                default -> System.out.println("Optiune invalida.");
            }
        }
    }

    private void createAppointment() throws Exception {
        String type = in.str("ServiceType (REVISION/REPAIR)");
        String dt = in.str("Data ora (format: 2026-01-08T18:30)");
        String vehicle = in.str("Info vehicul");
        String notes = in.str("Observatii initiale");

        Request req = new Request(Action.CLIENT_CREATE_APPOINTMENT);
        req.setToken(token);
        req.put("serviceType", type);
        req.put("scheduledAt", dt);
        req.put("vehicleInfo", vehicle);
        req.put("notes", notes);

        Response r = conn.send(req);
        print(r);
        if (r.isOk()) {
            Appointment a = (Appointment) r.getData().get("appointment");
            System.out.println("CREATED: id=" + a.getId() + ", cost=" + a.getEstimatedCost() + ", status=" + a.getStatus());
        }
    }

    private void listAppointments() throws Exception {
        Request req = new Request(Action.CLIENT_LIST_APPOINTMENTS);
        req.setToken(token);
        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            List<Appointment> list = (List<Appointment>) r.getData().get("appointments");
            if (list == null || list.isEmpty()) {
                System.out.println("Nu ai programari.");
                return;
            }
            for (Appointment a : list) {
                System.out.println("ID=" + a.getId()
                        + " | " + a.getServiceType()
                        + " | " + a.getScheduledAt()
                        + " | cost=" + a.getEstimatedCost()
                        + " | status=" + a.getStatus()
                        + " | vehicul=" + a.getVehicleInfo());
            }
        }
    }

    private void addNotes() throws Exception {
        long id = in.longVal("Appointment ID");
        String extra = in.str("Observatii de adaugat");

        Request req = new Request(Action.CLIENT_ADD_NOTES);
        req.setToken(token);
        req.getData().put("appointmentId", id);
        req.put("extraNotes", extra);

        Response r = conn.send(req);
        print(r);
    }

    private void notifications() throws Exception {
        Request req = new Request(Action.CLIENT_GET_NOTIFICATIONS);
        req.setToken(token);

        Response r = conn.send(req);
        print(r);

        if (r.isOk()) {
            @SuppressWarnings("unchecked")
            List<String> n = (List<String>) r.getData().get("notifications");
            if (n == null || n.isEmpty()) System.out.println("Nicio notificare.");
            else n.forEach(System.out::println);
        }
    }

    private void feedback() throws Exception {
        long id = in.longVal("Appointment ID");
        int rating = in.intVal("Rating (1..5)");
        String comment = in.str("Comentariu");

        Request req = new Request(Action.CLIENT_SUBMIT_FEEDBACK);
        req.setToken(token);
        req.getData().put("appointmentId", id);
        req.getData().put("rating", rating);
        req.put("comment", comment);

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