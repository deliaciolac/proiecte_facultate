package repository;

import database.Database;
import model.UsedPart;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsedPartRepository {

    public UsedPart insert(UsedPart up) throws SQLException {
        String sql = "INSERT INTO appointment_parts(appointment_id, inventory_item_id, part_name, quantity) VALUES(?,?,?,?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, up.getAppointmentId());

            if (up.getPartId() == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, up.getPartId());
            }

            ps.setString(3, up.getPartName());
            ps.setInt(4, up.getQuantity());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key (appointment_parts)");
                up.setId(keys.getLong(1));
                return up;
            }
        }
    }

    public List<UsedPart> listByAppointment(long appointmentId) throws SQLException {
        String sql = "SELECT id, appointment_id, inventory_item_id, part_name, quantity " +
                "FROM appointment_parts WHERE appointment_id=? ORDER BY id";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {
                List<UsedPart> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    private UsedPart map(ResultSet rs) throws SQLException {
        UsedPart up = new UsedPart();
        up.setId(rs.getLong("id"));
        up.setAppointmentId(rs.getLong("appointment_id"));

        long invId = rs.getLong("inventory_item_id");
        up.setPartId(rs.wasNull() ? null : invId);

        up.setPartName(rs.getString("part_name"));
        up.setQuantity(rs.getInt("quantity"));
        return up;
    }
}