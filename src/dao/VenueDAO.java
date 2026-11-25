package dao;

import database.DBConnection;
import model.Venue;
import repository.Repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VenueDAO implements Repository<Venue, String> {
    public void save(Venue venue) throws SQLException {
        String sql = "INSERT INTO venues (venue_id, name, city, capacity) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, city = ?, capacity = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, venue.getVenueId()); statement.setString(2, venue.getName()); statement.setString(3, venue.getCity()); statement.setInt(4, venue.getCapacity());
            statement.setString(5, venue.getName()); statement.setString(6, venue.getCity()); statement.setInt(7, venue.getCapacity()); statement.executeUpdate();
        }
    }
    public Optional<Venue> findById(String id) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT venue_id, name, city, capacity FROM venues WHERE venue_id = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(new Venue(result.getString("venue_id"), result.getString("name"), result.getString("city"), result.getInt("capacity"))) : Optional.empty(); }
        }
    }
    public List<Venue> findAll() throws SQLException {
        List<Venue> venues = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT venue_id, name, city, capacity FROM venues ORDER BY name"); ResultSet result = statement.executeQuery()) {
            while (result.next()) venues.add(new Venue(result.getString("venue_id"), result.getString("name"), result.getString("city"), result.getInt("capacity")));
        }
        return venues;
    }
    public boolean deleteById(String id) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM venues WHERE venue_id = ?")) { statement.setString(1, id); return statement.executeUpdate() == 1; }
    }
}
