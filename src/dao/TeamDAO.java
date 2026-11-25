package dao;

import database.DBConnection;
import model.Team;
import repository.Repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamDAO implements Repository<Team, String> {
    @Override
    public void save(Team team) throws SQLException {
        String sql = "INSERT INTO teams (team_id, name, city, coach) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, city = ?, coach = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, team.getTeamId()); statement.setString(2, team.getName()); statement.setString(3, team.getCity()); statement.setString(4, team.getCoach());
            statement.setString(5, team.getName()); statement.setString(6, team.getCity()); statement.setString(7, team.getCoach());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Team> findById(String id) throws SQLException {
        String sql = "SELECT t.team_id, t.name, t.city, t.coach, p.player_id AS captain_player_id, p.name AS captain_name, p.age AS captain_age, p.role AS captain_role, p.jersey_no AS captain_jersey_no FROM teams t LEFT JOIN players p ON t.captain_id = p.player_id WHERE t.team_id = ?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                return Optional.of(map(result));
            }
        }
    }

    @Override
    public List<Team> findAll() throws SQLException {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT t.team_id, t.name, t.city, t.coach, p.player_id AS captain_player_id, p.name AS captain_name, p.age AS captain_age, p.role AS captain_role, p.jersey_no AS captain_jersey_no FROM teams t LEFT JOIN players p ON t.captain_id = p.player_id ORDER BY t.name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) teams.add(map(result));
        }
        return teams;
    }

    @Override
    public boolean deleteById(String id) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM teams WHERE team_id = ?")) {
            statement.setString(1, id); return statement.executeUpdate() == 1;
        }
    }

    public void assignCaptain(String teamId, String playerId) throws SQLException {
        String sql = "UPDATE teams SET captain_id = ? WHERE team_id = ? AND EXISTS (SELECT 1 FROM players WHERE player_id = ? AND team_id = ?)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId); statement.setString(2, teamId); statement.setString(3, playerId); statement.setString(4, teamId);
            if (statement.executeUpdate() != 1) throw new SQLException("Captain must be a player on this team.");
        }
    }

    private Team map(ResultSet result) throws SQLException {
        Team team = new Team(result.getString("team_id"), result.getString("name"), result.getString("city"), result.getString("coach"));
        String captainId = result.getString("captain_player_id");
        if (captainId != null) team.appointCaptain(new model.Player(captainId, result.getString("captain_name"), result.getInt("captain_age"), model.PlayerRole.valueOf(result.getString("captain_role")), result.getInt("captain_jersey_no"), team.getTeamId()));
        return team;
    }
}
