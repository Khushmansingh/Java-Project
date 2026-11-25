package service;

import exception.InvalidResultException;
import model.Match;
import model.MatchStat;
import model.MatchStatus;
import model.Player;
import model.PointsEntry;
import rules.SportRules;
import database.DBConnection;
import dao.MatchStatDAO;
import dao.PointsTableDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultService {
    private final SportRules rules;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final PointsTableService pointsTableService;
    private final Map<String, Match> matchesById = new HashMap<>();
    private final Map<String, List<MatchStat>> statsByMatchId = new HashMap<>();

    public ResultService(SportRules rules, TeamService teamService, PlayerService playerService, PointsTableService pointsTableService) {
        this.rules = rules;
        this.teamService = teamService;
        this.playerService = playerService;
        this.pointsTableService = pointsTableService;
    }

    public void addMatch(Match match) { matchesById.put(match.getMatchId(), match); }

    public synchronized void recordResult(String matchId, int homeScore, int awayScore) {
        Match match = matchesById.get(matchId);
        if (match == null) throw new InvalidResultException("Match ID was not found: " + matchId);
        if (match.getStatus() != MatchStatus.SCHEDULED) throw new InvalidResultException("Match result was already entered or match is not scheduled.");
        if (homeScore < 0 || awayScore < 0) throw new InvalidResultException("Scores cannot be negative.");

        match.complete(homeScore, awayScore);
        PointsEntry home = pointsTableService.getOrCreate(match.getHomeTeamId());
        PointsEntry away = pointsTableService.getOrCreate(match.getAwayTeamId());
        boolean draw = homeScore == awayScore;
        home.recordResult(homeScore > awayScore, draw, homeScore > awayScore ? rules.pointsForWin() : draw ? rules.pointsForDrawOrTie() : rules.pointsForLoss(), homeScore - awayScore);
        away.recordResult(awayScore > homeScore, draw, awayScore > homeScore ? rules.pointsForWin() : draw ? rules.pointsForDrawOrTie() : rules.pointsForLoss(), awayScore - homeScore);
        statsByMatchId.putIfAbsent(matchId, new ArrayList<>());
    }

    public synchronized void recordResult(String matchId, int homeScore, int awayScore, List<MatchStat> stats) {
        Match match = matchesById.get(matchId);
        if (match == null) throw new InvalidResultException("Match ID was not found: " + matchId);
        for (MatchStat stat : stats) {
            if (!stat.getMatchId().equals(matchId)) throw new InvalidResultException("Stat belongs to a different match.");
            Player player = playerService.findById(stat.getPlayerId());
            if (!player.getTeamId().equals(match.getHomeTeamId()) && !player.getTeamId().equals(match.getAwayTeamId())) {
                throw new InvalidResultException("Stats can only be recorded for players in this match.");
            }
        }
        recordResult(matchId, homeScore, awayScore);
        statsByMatchId.put(matchId, new ArrayList<>(stats));
    }

    public Match findMatch(String matchId) { return matchesById.get(matchId); }
    public List<MatchStat> getStats(String matchId) { return new ArrayList<>(statsByMatchId.getOrDefault(matchId, List.of())); }
    public Map<String, Match> getMatches() { return new HashMap<>(matchesById); }

    public void recordResultInDatabase(String matchId, int homeScore, int awayScore, List<MatchStat> stats, boolean forceFailure) throws SQLException {
        if (homeScore < 0 || awayScore < 0) throw new InvalidResultException("Scores cannot be negative.");
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String homeTeamId;
                String awayTeamId;
                try (PreparedStatement select = connection.prepareStatement("SELECT home_team_id, away_team_id, status FROM matches WHERE match_id = ? FOR UPDATE")) {
                    select.setString(1, matchId);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) throw new InvalidResultException("Match ID was not found: " + matchId);
                        if (!"SCHEDULED".equals(result.getString("status"))) throw new InvalidResultException("Only a scheduled match can receive a result.");
                        homeTeamId = result.getString("home_team_id");
                        awayTeamId = result.getString("away_team_id");
                    }
                }
                String winnerId = homeScore == awayScore ? null : homeScore > awayScore ? homeTeamId : awayTeamId;
                String updateSql = "UPDATE matches SET home_score = ?, away_score = ?, winner_id = ?, status = 'COMPLETED' WHERE match_id = ? AND status = 'SCHEDULED'";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setInt(1, homeScore); update.setInt(2, awayScore); update.setString(3, winnerId); update.setString(4, matchId);
                    if (update.executeUpdate() != 1) throw new SQLException("Match result could not be updated.");
                }
                new MatchStatDAO().insertAll(connection, stats);
                String playerTeamSql = "SELECT team_id FROM players WHERE player_id = ?";
                try (PreparedStatement playerCheck = connection.prepareStatement(playerTeamSql)) {
                    for (MatchStat stat : stats) {
                        playerCheck.setString(1, stat.getPlayerId());
                        try (ResultSet playerResult = playerCheck.executeQuery()) {
                            if (!playerResult.next()) throw new InvalidResultException("Player ID was not found: " + stat.getPlayerId());
                            String playerTeamId = playerResult.getString("team_id");
                            if (!playerTeamId.equals(homeTeamId) && !playerTeamId.equals(awayTeamId)) throw new InvalidResultException("Player stats must belong to a team in this match.");
                        }
                    }
                }
                boolean draw = homeScore == awayScore;
                new PointsTableDAO().record(connection, homeTeamId, homeScore > awayScore, draw,
                        homeScore > awayScore ? rules.pointsForWin() : draw ? rules.pointsForDrawOrTie() : rules.pointsForLoss(), homeScore - awayScore);
                new PointsTableDAO().record(connection, awayTeamId, awayScore > homeScore, draw,
                        awayScore > homeScore ? rules.pointsForWin() : draw ? rules.pointsForDrawOrTie() : rules.pointsForLoss(), awayScore - homeScore);
                if (forceFailure) throw new SQLException("Forced transaction failure for rollback demonstration.");
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Result transaction failed and was rolled back.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
