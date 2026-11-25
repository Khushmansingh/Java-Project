package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PointsTableDAO {
    public List<String> orderedRows() throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT t.name, COALESCE(p.played, 0) AS played, COALESCE(p.won, 0) AS won, COALESCE(p.drawn, 0) AS drawn, COALESCE(p.lost, 0) AS lost, COALESCE(p.points, 0) AS points, COALESCE(p.score_difference, 0) AS score_difference FROM teams t LEFT JOIN points_table p ON p.team_id = t.team_id ORDER BY points DESC, score_difference DESC, t.name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) rows.add(csv(result.getString("name")) + "," + result.getInt("played") + "," + result.getInt("won") + "," + result.getInt("drawn") + "," + result.getInt("lost") + "," + result.getInt("points") + "," + result.getInt("score_difference"));
        }
        return rows;
    }

    public boolean deleteByTeam(String teamId) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM points_table WHERE team_id = ?")) { statement.setString(1, teamId); return statement.executeUpdate() == 1; }
    }

    private String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }

    public void record(Connection connection, String teamId, boolean win, boolean draw, int points, int scoreDifference) throws SQLException {
        String sql = "INSERT INTO points_table (team_id, played, won, drawn, lost, points, score_difference) VALUES (?, 1, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE played = played + 1, won = won + VALUES(won), drawn = drawn + VALUES(drawn), lost = lost + VALUES(lost), points = points + VALUES(points), score_difference = score_difference + VALUES(score_difference)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) { statement.setString(1, teamId); statement.setInt(2, win ? 1 : 0); statement.setInt(3, draw ? 1 : 0); statement.setInt(4, !win && !draw ? 1 : 0); statement.setInt(5, points); statement.setInt(6, scoreDifference); statement.executeUpdate(); }
    }
}
