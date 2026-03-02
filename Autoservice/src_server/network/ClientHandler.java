package network;

import dto.Action;
import dto.Request;
import dto.Response;
import model.*;
import repository.*;
import service.*;
import session.SessionManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final SessionManager sessions;

    private final UserRepository userRepo = new UserRepository();
    private final AppointmentRepository appRepo = new AppointmentRepository();
    private final PriceRepository priceRepo = new PriceRepository();
    private final FeedbackRepository feedbackRepo = new FeedbackRepository();
    private final PartRepository partRepo = new PartRepository();
    private final UsedPartRepository usedPartRepo = new UsedPartRepository();

    private final AuthService authService = new AuthService(userRepo);
    private final ClientService clientService = new ClientService(appRepo, priceRepo, feedbackRepo);
    private final MechanicService mechanicService = new MechanicService(appRepo, usedPartRepo, partRepo);
    private final ManagerService managerService =
            new ManagerService(appRepo, userRepo, priceRepo, partRepo, feedbackRepo);
    private final AdminService adminService = new AdminService(userRepo);

    public ClientHandler(Socket socket, SessionManager sessions) {
        this.socket = socket;
        this.sessions = sessions;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Request req)) {
                    out.writeObject(Response.fail("Request invalid."));
                    out.flush();
                    continue;
                }

                Response resp = handle(req);
                out.writeObject(resp);
                out.flush();
            }

        } catch (Exception ignored) {
            // client disconnect
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private Response handle(Request req) {
        try {
            return switch (req.getAction()) {
                // AUTH
                case REGISTER_CLIENT -> {
                    User u = authService.register(req.get("username", String.class), req.get("password", String.class), UserRole.CLIENT);
                    String token = sessions.create(u);
                    Response r = Response.ok("CLIENT creat + logat.");
                    r.setToken(token);
                    r.put("role", u.getRole().name());
                    yield r;
                }
                case REGISTER_MECHANIC -> {
                    User u = authService.register(req.get("username", String.class), req.get("password", String.class), UserRole.MECHANIC);
                    String token = sessions.create(u);
                    Response r = Response.ok("MECHANIC creat + logat.");
                    r.setToken(token);
                    r.put("role", u.getRole().name());
                    yield r;
                }
                case LOGIN -> {
                    User u = authService.login(req.get("username", String.class), req.get("password", String.class));
                    String token = sessions.create(u);
                    Response r = Response.ok("Login ok.");
                    r.setToken(token);
                    r.put("role", u.getRole().name());
                    yield r;
                }
                case LOGOUT -> {
                    sessions.remove(req.getToken());
                    yield Response.ok("Logout.");
                }

                // CLIENT
                case CLIENT_CREATE_APPOINTMENT -> {
                    User me = requireRole(req.getToken(), UserRole.CLIENT);
                    ServiceType type = ServiceType.valueOf(req.get("serviceType", String.class));
                    LocalDateTime dt = LocalDateTime.parse(req.get("scheduledAt", String.class)); // ISO-8601
                    String vehicle = req.get("vehicleInfo", String.class);
                    String notes = req.get("notes", String.class);

                    Appointment a = clientService.createAppointment(me.getId(), type, dt, vehicle, notes);
                    yield Response.ok("Programare creata.").put("appointment", a);
                }
                case CLIENT_LIST_APPOINTMENTS -> {
                    User me = requireRole(req.getToken(), UserRole.CLIENT);
                    List<Appointment> list = clientService.listAppointments(me.getId());
                    yield Response.ok("Lista programari.").put("appointments", list);
                }
                case CLIENT_ADD_NOTES -> {
                    User me = requireRole(req.getToken(), UserRole.CLIENT);
                    long appointmentId = (Long) req.getData().get("appointmentId");
                    String extra = req.get("extraNotes", String.class);
                    Appointment a = clientService.addNotes(me.getId(), appointmentId, extra);
                    yield Response.ok("Note adaugate.").put("appointment", a);
                }
                case CLIENT_GET_NOTIFICATIONS -> {
                    User me = requireRole(req.getToken(), UserRole.CLIENT);
                    var n = clientService.getNotifications(me.getId());
                    yield Response.ok("Notificari.").put("notifications", n);
                }
                case CLIENT_SUBMIT_FEEDBACK -> {
                    User me = requireRole(req.getToken(), UserRole.CLIENT);
                    long appointmentId = (Long) req.getData().get("appointmentId");
                    int rating = (Integer) req.getData().get("rating");
                    String comment = req.get("comment", String.class);
                    Feedback f = clientService.submitFeedback(me.getId(), appointmentId, rating, comment);
                    yield Response.ok("Feedback trimis.").put("feedback", f);
                }

                // MECHANIC
                case MECHANIC_LIST_ASSIGNED -> {
                    User me = requireRole(req.getToken(), UserRole.MECHANIC);
                    var list = mechanicService.listAssigned(me.getId());
                    yield Response.ok("Programari asignate.").put("appointments", list);
                }
                case MECHANIC_UPDATE_STATUS -> {
                    User me = requireRole(req.getToken(), UserRole.MECHANIC);
                    long appointmentId = (Long) req.getData().get("appointmentId");
                    WorkStatus status = WorkStatus.valueOf(req.get("status", String.class));
                    Appointment a = mechanicService.updateStatus(me.getId(), appointmentId, status);
                    yield Response.ok("Status actualizat.").put("appointment", a);
                }
                case MECHANIC_ADD_PARTS_AND_DURATION -> {
                    User me = requireRole(req.getToken(), UserRole.MECHANIC);
                    long appointmentId = (Long) req.getData().get("appointmentId");
                    int duration = (Integer) req.getData().get("durationMinutes");
                    String notes = req.get("mechanicNotes", String.class);

                    @SuppressWarnings("unchecked")
                    List<UsedPart> partsUsed = (List<UsedPart>) req.getData().get("partsUsed");

                    Appointment a = mechanicService.addPartsAndDuration(me.getId(), appointmentId, duration, notes, partsUsed);
                    yield Response.ok("Piese/durata salvate.").put("appointment", a);
                }
                case MECHANIC_LIST_PARTS -> {
                    requireRole(req.getToken(), UserRole.MECHANIC);
                    var list = mechanicService.listParts();
                    yield Response.ok("Lista inventar.").put("parts", list);
                }

                // MANAGER
                case MANAGER_LIST_PENDING -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    var list = managerService.listPending();
                    yield Response.ok("Pending unassigned.").put("appointments", list);
                }
                case MANAGER_ASSIGN_TO_MECHANIC -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    long appointmentId = (Long) req.getData().get("appointmentId");
                    long mechanicId = (Long) req.getData().get("mechanicId");
                    managerService.assignToMechanic(appointmentId, mechanicId);
                    yield Response.ok("Asignare ok.");
                }
                case MANAGER_LIST_MECHANICS -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    var mechs = managerService.listMechanics();
                    yield Response.ok("Lista mecanici.").put("mechanics", mechs);
                }
                case MANAGER_LIST_PRICES -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    var m = managerService.listPrices();
                    yield Response.ok("Lista preturi.").put("prices", m);
                }
                case MANAGER_LIST_FEEDBACK -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    var list = managerService.listFeedback();
                    yield Response.ok("Feedback.").put("feedback", list);
                }
                case MANAGER_SET_SERVICE_PRICE -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    ServiceType type = ServiceType.valueOf(req.get("serviceType", String.class));
                    double price = (Double) req.getData().get("price");
                    managerService.setServicePrice(type, price);
                    yield Response.ok("Pret setat.");
                }
                case MANAGER_LIST_PARTS -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    var list = managerService.listParts();
                    yield Response.ok("Lista piese.").put("parts", list);
                }
                case MANAGER_UPSERT_PART -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    Long id = (Long) req.getData().get("id"); // poate fi null
                    String name = req.get("name", String.class);
                    int stock = (Integer) req.getData().get("stock");
                    double unitPrice = (Double) req.getData().get("unitPrice");
                    Part p = managerService.upsertPart(id, name, stock, unitPrice);
                    yield Response.ok("Piesa salvata.").put("part", p);
                }
                case MANAGER_GET_STATS -> {
                    requireRole(req.getToken(), UserRole.MANAGER);
                    var stats = managerService.getStats();
                    yield Response.ok("Statistici.").put("stats", stats);
                }

                // ADMIN
                case ADMIN_LIST_USERS -> {
                    requireRole(req.getToken(), UserRole.ADMIN);
                    var list = adminService.listUsers();
                    yield Response.ok("Lista users.").put("users", list);
                }
                case ADMIN_ADD_MECHANIC -> {
                    requireRole(req.getToken(), UserRole.ADMIN);
                    User u = adminService.addMechanic(req.get("username", String.class), req.get("password", String.class));
                    yield Response.ok("Mecanic adaugat.").put("user", u);
                }
                case ADMIN_EDIT_MECHANIC -> {
                    requireRole(req.getToken(), UserRole.ADMIN);
                    long id = (Long) req.getData().get("mechanicId");
                    String newUsername = (String) req.getData().get("newUsername");
                    String newPassword = (String) req.getData().get("newPassword");

                    adminService.editMechanic(id, newUsername, newPassword);
                    yield Response.ok("Mecanic editat.");
                }

                case ADMIN_REMOVE_MECHANIC -> {
                    requireRole(req.getToken(), UserRole.ADMIN);
                    long id = (Long) req.getData().get("mechanicId");
                    adminService.removeMechanic(id);
                    yield Response.ok("Mecanic sters.");
                }
                case ADMIN_PROMOTE_CLIENT_TO_MECHANIC -> {
                    requireRole(req.getToken(), UserRole.ADMIN);
                    long id = (Long) req.getData().get("clientId");
                    adminService.promoteClientToMechanic(id);
                    yield Response.ok("Client promovat la mecanic.");
                }
                case ADMIN_PROMOTE_CLIENT_TO_MANAGER -> {
                    requireRole(req.getToken(), UserRole.ADMIN);
                    long id = (Long) req.getData().get("clientId");
                    adminService.promoteClientToManager(id);
                    yield Response.ok("Client promovat la manager.");
                }
            };
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("Eroare server: " + e.getMessage());
        }
    }

    private User requireRole(String token, UserRole role) {
        User u = sessions.get(token);
        if (u == null) throw new ServiceException("Neautentificat (token invalid).");
        if (u.getRole() != role) throw new ServiceException("Acces interzis. Rol necesar: " + role);
        return u;
    }
}