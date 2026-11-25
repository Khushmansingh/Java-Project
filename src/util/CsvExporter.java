package util;

import dao.MatchDAO;
import dao.PlayerDAO;
import dao.PointsTableDAO;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class CsvExporter {
    private final MatchDAO matchDAO;
    private final PlayerDAO playerDAO;
    private final PointsTableDAO pointsTableDAO;
    public CsvExporter(MatchDAO matchDAO, PlayerDAO playerDAO, PointsTableDAO pointsTableDAO) { this.matchDAO = matchDAO; this.playerDAO = playerDAO; this.pointsTableDAO = pointsTableDAO; }

    public void exportPointsTable(Path path) throws IOException, SQLException { write(path, "team,played,won,drawn,lost,points,score_difference", pointsTableDAO.orderedRows()); }
    public void exportFixtures(Path path) throws IOException, SQLException {
        List<String> rows = matchDAO.findAll().stream().map(match -> match.getMatchId() + "," + match.getDate() + "," + match.getTime() + "," + match.getHomeTeamId() + "," + match.getAwayTeamId() + "," + match.getStatus()).toList();
        write(path, "match_id,date,time,home_team,away_team,status", rows);
    }
    public void exportLeaderboard(Path path) throws IOException, SQLException {
        write(path, "player_id,name,team_id,total_stat", playerDAO.leaderboardRows());
    }
    private void write(Path path, String heading, List<String> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(heading);
            writer.newLine();
            for (String row : rows) { writer.write(row); writer.newLine(); }
        }
    }
}
