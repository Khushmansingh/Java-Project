package service;

import concurrency.ResultConsumer;
import concurrency.ResultProducer;
import concurrency.ResultQueue;
import concurrency.ResultTask;
import dao.MatchDAO;
import dao.PlayerDAO;
import dao.PointsTableDAO;
import dao.TeamDAO;
import dao.VenueDAO;
import exception.InvalidInputException;
import model.Match;
import model.MatchStat;
import model.Player;
import model.PlayerRole;
import model.Team;
import model.Venue;
import rules.SportRules;
import util.CsvExporter;
import util.CsvImporter;
import util.CsvImportReport;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminConsoleService {
    private final TeamService teamService = new TeamService();
    private final PlayerService playerService = new PlayerService();
    private final VenueService venueService = new VenueService(new VenueDAO());
    private final MatchService matchService = new MatchService(new MatchDAO(), new concurrency.VenueSlotManager());
    private final ReportService reportService = new ReportService(new MatchDAO(), new PlayerDAO());
    private final ResultService resultService;
    private ResultQueue pendingQueue = new ResultQueue();
    private ResultProducer resultProducer = new ResultProducer(pendingQueue);
    private final SportRules rules;

    public AdminConsoleService(SportRules rules) {
        this.rules = rules;
        this.resultService = new ResultService(rules, teamService, playerService, new PointsTableService());
    }

    public List<Team> teams() throws SQLException { return teamService.listTeamsFromDatabase(); }
    public void saveTeam(Team team) throws SQLException { teamService.saveTeamToDatabase(team); }
    public void assignCaptain(String teamId, String playerId) throws SQLException { teamService.assignCaptainToDatabase(teamId, playerId); }
    public void deleteTeam(String id) throws SQLException { teamService.deleteTeamFromDatabase(id); }
    public List<Player> players() throws SQLException { return playerService.listPlayersFromDatabase(); }
    public void savePlayer(Player player) throws SQLException { playerService.savePlayerToDatabase(player); }
    public void deletePlayer(String id) throws SQLException { playerService.deletePlayerFromDatabase(id); }
    public Player findPlayer(String id) throws SQLException { return new PlayerDAO().findById(id).orElseThrow(() -> new InvalidInputException("Player ID was not found: " + id)); }
    public List<Player> searchPlayers(String text) throws SQLException { return playerService.searchDatabase(text); }
    public List<Venue> venues() throws SQLException { return venueService.listVenues(); }
    public void saveVenue(Venue venue) throws SQLException { venueService.addVenue(venue); }
    public void schedule(Match match) throws SQLException { matchService.schedule(match); }
    public List<Match> matches() throws SQLException { return matchService.listMatches(); }
    public List<String> teamHistory(String teamId) throws SQLException { return reportService.teamMatchHistory(teamId); }
    public List<String> upcoming() throws SQLException { return reportService.upcomingFixtures(); }
    public List<String> pointsTable() throws SQLException { return new PointsTableDAO().orderedRows(); }
    public List<String> leaderboard() throws SQLException {
        PlayerDAO playerDAO = new PlayerDAO();
        List<String> rows = new ArrayList<>();
        rows.add("Top scorers (" + rules.getStatLabel() + "):");
        rows.addAll(playerDAO.leaderboardRows());
        if (rules.getStatLabel().equals("Runs")) { rows.add("Best bowlers (wickets):"); rows.addAll(playerDAO.bestBowlers()); }
        rows.add("MVP (highest total primary stat contribution):");
        if (playerDAO.leaderboardRows().size() > 0) rows.add(playerDAO.leaderboardRows().get(0));
        return rows;
    }

    public void enqueueResult(String matchId, int homeScore, int awayScore, List<MatchStat> stats) throws InterruptedException {
        ResultTask task = new ResultTask(() -> {
            try { resultService.recordResultInDatabase(matchId, homeScore, awayScore, stats, false); }
            catch (Exception exception) { System.out.println("Result " + matchId + " was not saved: " + exception.getMessage()); }
        });
        resultProducer.submit(task);
    }

    public int processPendingResults() throws InterruptedException {
        int count = pendingQueue.size();
        Thread consumer = new Thread(new ResultConsumer(pendingQueue), "ResultConsumer");
        consumer.start();
        pendingQueue.close();
        consumer.join();
        pendingQueue = new ResultQueue();
        resultProducer = new ResultProducer(pendingQueue);
        return count;
    }

    public CsvImportReport importPlayers(Path path) throws SQLException, IOException {
        TeamService csvTeams = new TeamService();
        PlayerService csvPlayers = new PlayerService();
        TeamDAO teamDAO = new TeamDAO();
        PlayerDAO playerDAO = new PlayerDAO();
        for (Team team : teamDAO.findAll()) csvTeams.addTeam(team);
        for (Player player : playerDAO.findAll()) csvPlayers.addPlayer(player, csvTeams.findById(player.getTeamId()));
        return new CsvImporter(csvPlayers, csvTeams, playerDAO).importPlayers(path);
    }

    public void exportAll(Path directory) throws SQLException, IOException {
        CsvExporter exporter = new CsvExporter(new MatchDAO(), new PlayerDAO(), new PointsTableDAO());
        java.nio.file.Files.createDirectories(directory);
        exporter.exportPointsTable(directory.resolve("points_table.csv"));
        exporter.exportFixtures(directory.resolve("fixtures.csv"));
        exporter.exportLeaderboard(directory.resolve("leaderboard.csv"));
    }

    public List<MatchStat> readStats(String matchId, List<String> playerIds, List<Integer> contributions) {
        if (playerIds.size() != contributions.size()) throw new InvalidInputException("Player and stat counts do not match.");
        List<MatchStat> stats = new ArrayList<>();
        for (int index = 0; index < playerIds.size(); index++) stats.add(new MatchStat(matchId, playerIds.get(index), contributions.get(index), 0));
        return stats;
    }
}
