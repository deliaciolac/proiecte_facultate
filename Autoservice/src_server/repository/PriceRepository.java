package repository;

import database.Database;
import model.ServiceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PriceRepository {

    public double getBasePrice(ServiceType type) throws SQLException {
        String sql = "SELECT base_price FROM service_prices WHERE service_type=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                return rs.getDouble("base_price");
            }
        }
    }

    public java.util.Map<ServiceType, Double> listPrices() throws SQLException {
        String sql = "SELECT service_type, base_price FROM service_prices";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            java.util.Map<ServiceType, Double> out = new java.util.HashMap<>();
            while (rs.next()) {
                ServiceType type = ServiceType.valueOf(rs.getString("service_type"));
                double price = rs.getDouble("base_price");
                out.put(type, price);
            }
            return out;
        }
    }

    public int setBasePrice(ServiceType type, double price) throws SQLException {
        String sql = "INSERT INTO service_prices(service_type, base_price) VALUES(?,?) " +
                "ON DUPLICATE KEY UPDATE base_price=VALUES(base_price)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ps.setDouble(2, price);
            return ps.executeUpdate();
        }
    }
}