package repository;

import database.Database;
import model.Feedback;
import model.FeedbackView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRepository {

    public Feedback findByAppointmentId(long appointmentId) throws SQLException {
        String sql = "SELECT * FROM feedback WHERE appointment_id=? LIMIT 1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Feedback f = new Feedback();
                f.setId(rs.getLong("id"));
                f.setAppointmentId(rs.getLong("appointment_id"));
                f.setClientId(rs.getLong("client_id"));
                f.setRating(rs.getInt("rating"));
                f.setComment(rs.getString("comment"));
                return f;
            }
        }
    }

    public Feedback insert(long appointmentId, long clientId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO feedback(appointment_id, client_id, rating, comment) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, appointmentId);
            ps.setLong(2, clientId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key (feedback)");
                Feedback f = new Feedback();
                f.setId(keys.getLong(1));
                f.setAppointmentId(appointmentId);
                f.setClientId(clientId);
                f.setRating(rating);
                f.setComment(comment);
                return f;
            }
        }
    }

    /**
     * Pentru MANAGER: lista cu username + created_at
     */
    public List<FeedbackView> listAllForManager() throws SQLException {
        String sql = """
            SELECT f.appointment_id, f.client_id, u.username, f.rating, f.comment, f.created_at
            FROM feedback f
            JOIN users u ON u.id = f.client_id
            ORDER BY f.created_at DESC
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<FeedbackView> out = new ArrayList<>();
            while (rs.next()) {
                FeedbackView v = new FeedbackView();
                v.setAppointmentId(rs.getLong("appointment_id"));
                v.setClientId(rs.getLong("client_id"));
                v.setClientUsername(rs.getString("username"));
                v.setRating(rs.getInt("rating"));
                v.setComment(rs.getString("comment"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) v.setCreatedAt(ts.toLocalDateTime());

                out.add(v);
            }
            return out;
        }
    }
}