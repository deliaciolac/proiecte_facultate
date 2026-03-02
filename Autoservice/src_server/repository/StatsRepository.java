package repository;

import database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class StatsRepository {

    public Map<String, Object> getStats() throws Exception {
        Map<String, Object> out = new LinkedHashMap<>();

        // total programari
        out.put("totalAppointments", oneLong("SELECT COUNT(*) FROM appointments"));

        // finalizate
        out.put("completedAppointments", oneLong(
                "SELECT COUNT(*) FROM appointments WHERE status='COMPLETED'"));

        // incasari (sum estimated_cost pe COMPLETED)
        out.put("totalRevenue", oneDouble(
                "SELECT COALESCE(SUM(estimated_cost),0) FROM appointments WHERE status='COMPLETED'"));

        // distributie tip serviciu (REVISION / REPAIR) pe COMPLETED
        out.put("revisionCount", oneLong(
                "SELECT COUNT(*) FROM appointments WHERE status='COMPLETED' AND service_type='REVISION'"));
        out.put("repairCount", oneLong(
                "SELECT COUNT(*) FROM appointments WHERE status='COMPLETED' AND service_type='REPAIR'"));

        // performanta echipei
        out.put("teamPerformance", teamPerformance());

        return out;
    }

    private List<Map<String, Object>> teamPerformance() throws Exception {
        String sql = """
                SELECT u.id AS mechanicId, u.username AS mechanicUsername,
                       COUNT(a.id) AS completedCount,
                       COALESCE(SUM(a.estimated_cost),0) AS revenue
                FROM users u
                LEFT JOIN appointments a
                  ON a.mechanic_id = u.id AND a.status='COMPLETED'
                WHERE u.role='MECHANIC'
                GROUP BY u.id, u.username
                ORDER BY completedCount DESC, revenue DESC
                """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
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

    private long oneLong(String sql) throws Exception {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private double oneDouble(String sql) throws Exception {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }
}