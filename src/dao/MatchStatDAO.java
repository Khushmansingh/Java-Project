package dao;

import database.DBConnection;
import model.MatchStat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class MatchStatDAO {
    public void insertAll(Connection connection, List<MatchStat> stats) throws SQLException {
        String sql = "INSERT INTO match_stats (match_id, player_id, score_contribution, secondary_contribution) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (MatchStat stat : stats) { statement.setString(1, stat.getMatchId()); statement.setString(2, stat.getPlayerId()); statement.setInt(3, stat.getScoreContribution()); statement.setInt(4, stat.getSecondaryContribution()); statement.addBatch(); }
            statement.executeBatch();
        }
    }
    public void saveAll(List<MatchStat> stats) throws SQLException { try (Connection connection = DBConnection.getConnection()) { insertAll(connection, stats); } }
    public List<MatchStat> findByMatch(String matchId) throws SQLException {
        List<MatchStat> stats = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT match_id, player_id, score_contribution, secondary_contribution FROM match_stats WHERE match_id = ?")) { statement.setString(1, matchId); try (ResultSet result = statement.executeQuery()) { while (result.next()) stats.add(new MatchStat(result.getString("match_id"), result.getString("player_id"), result.getInt("score_contribution"), result.getInt("secondary_contribution"))); } }
        return stats;
    }
    public int deleteByMatch(String matchId) throws SQLException { try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM match_stats WHERE match_id = ?")) { statement.setString(1, matchId); return statement.executeUpdate(); } }
}
