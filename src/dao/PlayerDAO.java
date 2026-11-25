package dao;

import database.DBConnection;
import model.Player;
import model.PlayerRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO {
    public void save(Player player) throws SQLException {
        String sql = "INSERT INTO players (player_id, name, age, role, jersey_no, team_id) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, age = ?, role = ?, jersey_no = ?, team_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getPlayerId()); statement.setString(2, player.getName()); statement.setInt(3, player.getAge()); statement.setString(4, player.getRole().name()); statement.setInt(5, player.getJerseyNumber()); statement.setString(6, player.getTeamId());
            statement.setString(7, player.getName()); statement.setInt(8, player.getAge()); statement.setString(9, player.getRole().name()); statement.setInt(10, player.getJerseyNumber()); statement.setString(11, player.getTeamId()); statement.executeUpdate();
        }
    }
    public Optional<Player> findById(String id) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT player_id, name, age, role, jersey_no, team_id FROM players WHERE player_id = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(map(result)) : Optional.empty(); }
        }
    }
    public List<Player> findAll() throws SQLException {
        List<Player> players = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT player_id, name, age, role, jersey_no, team_id FROM players ORDER BY name"); ResultSet result = statement.executeQuery()) { while (result.next()) players.add(map(result)); }
        return players;
    }
    public List<Player> findByNameOrTeam(String search) throws SQLException {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT p.player_id, p.name, p.age, p.role, p.jersey_no, p.team_id FROM players p JOIN teams t ON p.team_id = t.team_id WHERE p.name LIKE ? OR t.name LIKE ? ORDER BY p.name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, "%" + search + "%"); statement.setString(2, "%" + search + "%"); try (ResultSet result = statement.executeQuery()) { while (result.next()) players.add(map(result)); } }
        return players;
    }
    public List<String> leaderboardRows() throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT p.player_id, p.name, p.team_id, COALESCE(SUM(s.score_contribution), 0) AS total_stat FROM players p LEFT JOIN match_stats s ON p.player_id = s.player_id GROUP BY p.player_id, p.name, p.team_id ORDER BY total_stat DESC, p.name LIMIT 5";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) { while (result.next()) rows.add(result.getString("player_id") + ",\"" + result.getString("name").replace("\"", "\"\"") + "\"," + result.getString("team_id") + "," + result.getInt("total_stat")); }
        return rows;
    }
    public List<String> bestBowlers() throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT p.player_id, p.name, p.team_id, COALESCE(SUM(s.secondary_contribution), 0) AS wickets FROM players p LEFT JOIN match_stats s ON p.player_id = s.player_id WHERE p.role = 'BOWLER' GROUP BY p.player_id, p.name, p.team_id ORDER BY wickets DESC, p.name LIMIT 5";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) { while (result.next()) rows.add(result.getString("player_id") + ",\"" + result.getString("name").replace("\"", "\"\"") + "\"," + result.getString("team_id") + "," + result.getInt("wickets")); }
        return rows;
    }
    public int countByTeam(String teamId) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM players WHERE team_id = ?")) { statement.setString(1, teamId); try (ResultSet result = statement.executeQuery()) { result.next(); return result.getInt(1); } }
    }
    public boolean jerseyExists(String teamId, int jerseyNumber, String exceptPlayerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM players WHERE team_id = ? AND jersey_no = ? AND player_id <> ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, teamId); statement.setInt(2, jerseyNumber); statement.setString(3, exceptPlayerId == null ? "" : exceptPlayerId); try (ResultSet result = statement.executeQuery()) { result.next(); return result.getInt(1) > 0; } }
    }
    public boolean deleteById(String id) throws SQLException { try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM players WHERE player_id = ?")) { statement.setString(1, id); return statement.executeUpdate() == 1; } }
    private Player map(ResultSet result) throws SQLException { return new Player(result.getString("player_id"), result.getString("name"), result.getInt("age"), PlayerRole.valueOf(result.getString("role")), result.getInt("jersey_no"), result.getString("team_id")); }
}
