package service;

import database.Database;
import model.*;
import repository.AppointmentRepository;
import repository.FeedbackRepository;
import repository.PartRepository;
import repository.PriceRepository;
import repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ManagerService {
    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final PriceRepository prices;
    private final PartRepository parts;
    private final FeedbackRepository feedback;

    public ManagerService(AppointmentRepository appointments,
                          UserRepository users,
                          PriceRepository prices,
                          PartRepository parts,
                          FeedbackRepository feedback) {
        this.appointments = appointments;
        this.users = users;
        this.prices = prices;
        this.parts = parts;
        this.feedback = feedback;
    }

    public List<Appointment> listPending() {
        try {
            return appointments.listPendingUnassigned();
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public void assignToMechanic(long appointmentId, long mechanicId) {
        try {
            var mech = users.findById(mechanicId);
            if (mech == null) throw new ServiceException("Mecanic inexistent.");
            if (mech.getRole() != UserRole.MECHANIC) throw new ServiceException("User-ul ales nu e MECHANIC.");

            int updated = appointments.assignMechanic(appointmentId, mechanicId);
            if (updated == 0) throw new ServiceException("Nu se poate asigna (poate deja asignata sau inexistenta).");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public void setServicePrice(ServiceType type, double price) {
        if (price < 0) throw new ServiceException("Pret invalid.");
        try {
            prices.setBasePrice(type, price);
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public Map<ServiceType, Double> listPrices() {
        try {
            return prices.listPrices();
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public List<Part> listParts() {
        try {
            return parts.listAll();
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public Part upsertPart(Long id, String name, int stock, double unitPrice) {
        if (name == null || name.isBlank()) throw new ServiceException("Nume piesa invalid.");
        if (stock < 0) throw new ServiceException("Stoc invalid.");
        if (unitPrice < 0) throw new ServiceException("Pret invalid.");

        try {
            String nm = name.trim();

            if (id == null) {
                Part existing = parts.findByName(nm);
                if (existing != null) {
                    parts.update(existing.getId(), nm, stock, unitPrice);
                    existing.setName(nm);
                    existing.setStock(stock);
                    existing.setUnitPrice(unitPrice);
                    return existing;
                }
                return parts.insert(nm, stock, unitPrice);
            } else {
                int updated = parts.update(id, nm, stock, unitPrice);
                if (updated == 0) throw new ServiceException("Piesa inexistenta.");

                Part p = new Part();
                p.setId(id);
                p.setName(nm);
                p.setStock(stock);
                p.setUnitPrice(unitPrice);
                return p;
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }


    public List<FeedbackView> listFeedback() {
        try {
            return feedback.listAllForManager();
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public List<User> listMechanics() {
        try {
            return users.listByRole(UserRole.MECHANIC);
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }


    public Map<String, Object> getStats() {
        try (Connection c = Database.getConnection()) {
            Map<String, Object> m = new LinkedHashMap<>();

            m.put("totalAppointments", oneLong(c, "SELECT COUNT(*) FROM appointments"));
            m.put("pendingAppointments", oneLong(c, "SELECT COUNT(*) FROM appointments WHERE status='PENDING'"));
            m.put("inProgressAppointments", oneLong(c, "SELECT COUNT(*) FROM appointments WHERE status='IN_PROGRESS'"));
            m.put("completedAppointments", oneLong(c, "SELECT COUNT(*) FROM appointments WHERE status='COMPLETED'"));

            m.put("totalRevenue", oneDouble(c,
                    "SELECT COALESCE(SUM(estimated_cost),0) FROM appointments WHERE status='COMPLETED'"));

            m.put("completedRevisionCount", oneLong(c,
                    "SELECT COUNT(*) FROM appointments WHERE status='COMPLETED' AND service_type='REVISION'"));
            m.put("completedRepairCount", oneLong(c,
                    "SELECT COUNT(*) FROM appointments WHERE status='COMPLETED' AND service_type='REPAIR'"));

            m.put("teamPerformance", teamPerformance(c));

            return m;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    private long oneLong(Connection c, String sql) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private double oneDouble(Connection c, String sql) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    private List<Map<String, Object>> teamPerformance(Connection c) throws Exception {
        String sql = """
                SELECT u.id AS mechanicId,
                       u.username AS mechanicUsername,
                       COUNT(a.id) AS completedCount,
                       COALESCE(SUM(a.estimated_cost),0) AS revenue
                FROM users u
                LEFT JOIN appointments a
                  ON a.mechanic_id = u.id AND a.status='COMPLETED'
                WHERE u.role='MECHANIC'
                GROUP BY u.id, u.username
                ORDER BY completedCount DESC, revenue DESC, mechanicUsername ASC
                """;

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("mechanicId", rs.getLong("mechanicId"));
                row.put("mechanicUsername", rs.getString("mechanicUsername"));
                row.put("completedCount", rs.getLong("completedCount"));
                row.put("revenue", rs.getDouble("revenue"));
                list.add(row);
            }
            return list;
        }
    }
}