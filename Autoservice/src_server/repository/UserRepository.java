package repository;

import database.Database;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public boolean existsUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE lower(username)=lower(?) LIMIT 1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public User insert(String username, String passwordHash, UserRole role) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key (users)");
                long id = keys.getLong(1);
                return new User(id, username, passwordHash, role);
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE lower(username)=lower(?) LIMIT 1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int updatePassword(long id, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
            return ps.executeUpdate();
        }
    }


    public User findById(long id) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE id=? LIMIT 1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public List<User> listAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users ORDER BY id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public List<User> listByRole(UserRole role) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE role=? ORDER BY id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<User> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public int updateUsername(long userId, String newUsername) throws SQLException {
        String sql = "UPDATE users SET username=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setLong(2, userId);
            return ps.executeUpdate();
        }
    }

    public int updateRole(long userId, UserRole role) throws SQLException {
        String sql = "UPDATE users SET role=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setLong(2, userId);
            return ps.executeUpdate();
        }
    }

    public int delete(long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role"))
        );
    }
}