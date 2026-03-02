package repository;

import database.Database;
import model.Appointment;
import model.ServiceType;
import model.WorkStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    public Appointment insert(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments(client_id, mechanic_id, service_type, scheduled_at, vehicle_info, client_notes, estimated_cost, status) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, a.getClientId());
            if (a.getMechanicId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, a.getMechanicId());
            ps.setString(3, a.getServiceType().name());
            ps.setTimestamp(4, Timestamp.valueOf(a.getScheduledAt()));
            ps.setString(5, a.getVehicleInfo());
            ps.setString(6, a.getClientNotes());
            ps.setDouble(7, a.getEstimatedCost());
            ps.setString(8, a.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key (appointments)");
                a.setId(keys.getLong(1));
                return a;
            }
        }
    }

    public Appointment findById(long id) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public List<Appointment> listByClient(long clientId) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE client_id=? ORDER BY scheduled_at";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public List<Appointment> listAssignedToMechanic(long mechanicId) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE mechanic_id=? ORDER BY scheduled_at";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, mechanicId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public List<Appointment> listPendingUnassigned() throws SQLException {
        String sql = "SELECT * FROM appointments WHERE mechanic_id IS NULL AND status='PENDING' ORDER BY scheduled_at";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Appointment> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public int assignMechanic(long appointmentId, long mechanicId) throws SQLException {
        String sql = "UPDATE appointments SET mechanic_id=? WHERE id=? AND mechanic_id IS NULL";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, mechanicId);
            ps.setLong(2, appointmentId);
            return ps.executeUpdate();
        }
    }

    public int updateClientNotes(long appointmentId, String notes) throws SQLException {
        String sql = "UPDATE appointments SET client_notes=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setLong(2, appointmentId);
            return ps.executeUpdate();
        }
    }

    public int updateMechanicWork(long appointmentId, WorkStatus status, int durationMinutes, String mechanicNotes) throws SQLException {
        String sql = "UPDATE appointments SET status=?, duration_minutes=?, mechanic_notes=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, durationMinutes);
            ps.setString(3, mechanicNotes);
            ps.setLong(4, appointmentId);
            return ps.executeUpdate();
        }
    }

    public int markCompletionNotified(long appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET completion_notified=TRUE WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            return ps.executeUpdate();
        }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getLong("id"));
        a.setClientId(rs.getLong("client_id"));

        long mech = rs.getLong("mechanic_id");
        a.setMechanicId(rs.wasNull() ? null : mech);

        a.setServiceType(ServiceType.valueOf(rs.getString("service_type")));
        Timestamp ts = rs.getTimestamp("scheduled_at");
        a.setScheduledAt(ts == null ? LocalDateTime.now() : ts.toLocalDateTime());

        a.setVehicleInfo(rs.getString("vehicle_info"));
        a.setClientNotes(rs.getString("client_notes"));

        a.setEstimatedCost(rs.getDouble("estimated_cost"));
        a.setStatus(WorkStatus.valueOf(rs.getString("status")));

        a.setDurationMinutes(rs.getInt("duration_minutes"));
        a.setMechanicNotes(rs.getString("mechanic_notes"));

        a.setCompletionNotified(rs.getBoolean("completion_notified"));
        return a;
    }
}