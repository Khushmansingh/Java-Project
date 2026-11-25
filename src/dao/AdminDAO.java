package dao;

import database.DBConnection;
import model.Admin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDAO {
    public void save(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO admins (username, password_hash) VALUES (?, ?) ON DUPLICATE KEY UPDATE password_hash = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, username); statement.setString(2, passwordHash); statement.setString(3, passwordHash); statement.executeUpdate(); }
    }
    public Optional<Admin> findByUsername(String username) throws SQLException {
        String sql = "SELECT username, password_hash FROM admins WHERE username = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                return Optional.of(new Admin(result.getString("username"), result.getString("password_hash")));
            }
        }
    }
    public boolean deleteByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM admins WHERE username = ?")) { statement.setString(1, username); return statement.executeUpdate() == 1; }
    }
}
