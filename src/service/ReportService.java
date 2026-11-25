package service;

import dao.MatchDAO;
import dao.PlayerDAO;
import java.sql.SQLException;
import java.util.List;

public class ReportService {
    private final MatchDAO matchDAO;
    private final PlayerDAO playerDAO;
    public ReportService(MatchDAO matchDAO, PlayerDAO playerDAO) { this.matchDAO = matchDAO; this.playerDAO = playerDAO; }
    public List<String> teamMatchHistory(String teamId) throws SQLException { return matchDAO.historyByTeam(teamId); }
    public List<model.Player> searchPlayers(String term) throws SQLException { return playerDAO.findByNameOrTeam(term); }
    public List<String> upcomingFixtures() throws SQLException {
        return matchDAO.findAll().stream().filter(match -> match.getStatus() == model.MatchStatus.SCHEDULED).map(match -> match.getMatchId() + " " + match.getDate() + " " + match.getHomeTeamId() + " vs " + match.getAwayTeamId()).toList();
    }
}
