package dao;

import database.DBConnection;
import model.Match;
import model.MatchStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDAO {
    public void save(Match match) throws SQLException {
        String sql = "INSERT INTO matches (match_id, home_team_id, away_team_id, venue_id, match_date, match_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, match.getMatchId()); statement.setString(2, match.getHomeTeamId()); statement.setString(3, match.getAwayTeamId()); statement.setString(4, match.getVenueId()); statement.setDate(5, java.sql.Date.valueOf(match.getDate())); statement.setTime(6, java.sql.Time.valueOf(match.getTime())); statement.setString(7, match.getStatus().name()); statement.executeUpdate(); }
    }
    public Optional<Match> findById(String id) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM matches WHERE match_id = ?")) { statement.setString(1, id); try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(map(result)) : Optional.empty(); } }
    }
    public List<Match> findAll() throws SQLException {
        List<Match> matches = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM matches ORDER BY match_date, match_time"); ResultSet result = statement.executeQuery()) { while (result.next()) matches.add(map(result)); }
        return matches;
    }
    public List<String> historyByTeam(String teamId) throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT m.match_id, m.match_date, m.status, h.name AS home_name, a.name AS away_name, m.home_score, m.away_score FROM matches m JOIN teams h ON m.home_team_id = h.team_id JOIN teams a ON m.away_team_id = a.team_id WHERE m.home_team_id = ? OR m.away_team_id = ? ORDER BY m.match_date";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, teamId); statement.setString(2, teamId); try (ResultSet result = statement.executeQuery()) { while (result.next()) rows.add(result.getString("match_id") + " " + result.getDate("match_date") + " " + result.getString("home_name") + " " + result.getObject("home_score") + "-" + result.getObject("away_score") + " " + result.getString("away_name") + " [" + result.getString("status") + "]"); } }
        return rows;
    }
    public boolean deleteById(String id) throws SQLException { try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM matches WHERE match_id = ?")) { statement.setString(1, id); return statement.executeUpdate() == 1; } }
    public boolean updateSchedule(String matchId, String venueId, LocalDate date, LocalTime time) throws SQLException {
        String sql = "UPDATE matches SET venue_id = ?, match_date = ?, match_time = ? WHERE match_id = ? AND status = 'SCHEDULED'";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, venueId); statement.setDate(2, java.sql.Date.valueOf(date)); statement.setTime(3, java.sql.Time.valueOf(time)); statement.setString(4, matchId); return statement.executeUpdate() == 1; }
    }
    private Match map(ResultSet result) throws SQLException {
        Match match = new Match(result.getString("match_id"), result.getString("home_team_id"), result.getString("away_team_id"), result.getString("venue_id"), result.getDate("match_date").toLocalDate(), result.getTime("match_time").toLocalTime());
        if (MatchStatus.COMPLETED.name().equals(result.getString("status"))) match.complete(result.getInt("home_score"), result.getInt("away_score"));
        else if (MatchStatus.CANCELLED.name().equals(result.getString("status"))) match.cancel();
        return match;
    }
}
