package repository;

import database.Database;
import model.Part;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartRepository {

    public List<Part> listAll() throws SQLException {
        String sql = "SELECT id, name, stock, unit_price FROM parts ORDER BY name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Part> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public Part findById(long id) throws SQLException {
        String sql = "SELECT id, name, stock, unit_price FROM parts WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Part findByName(String name) throws SQLException {
        String sql = "SELECT id, name, stock, unit_price FROM parts WHERE lower(name)=lower(?) LIMIT 1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Part insert(String name, int stock, double unitPrice) throws SQLException {
        String sql = "INSERT INTO parts(name, stock, unit_price) VALUES(?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, stock);
            ps.setDouble(3, unitPrice);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key (parts)");
                long id = keys.getLong(1);
                Part p = new Part();
                p.setId(id); p.setName(name); p.setStock(stock); p.setUnitPrice(unitPrice);
                return p;
            }
        }
    }

    public int update(long id, String name, int stock, double unitPrice) throws SQLException {
        String sql = "UPDATE parts SET name=?, stock=?, unit_price=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, stock);
            ps.setDouble(3, unitPrice);
            ps.setLong(4, id);
            return ps.executeUpdate();
        }
    }

    public int decreaseStock(long partId, int qty) throws SQLException {
        String sql = "UPDATE parts SET stock = stock - ? WHERE id=? AND stock >= ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setLong(2, partId);
            ps.setInt(3, qty);
            return ps.executeUpdate();
        }
    }

    private Part map(ResultSet rs) throws SQLException {
        Part p = new Part();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setStock(rs.getInt("stock"));
        p.setUnitPrice(rs.getDouble("unit_price"));
        return p;
    }
}