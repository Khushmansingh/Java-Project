package main;                                                                                                                                                                                                                         import dao.AdminDAO;
                                                                                                                                                                                                                             import exception.InvalidInputException;import model.Match;import model.MatchStat;import model.Player;import model.PlayerRole;import model.Team;import model.Venue;import model.Sport;import rules.CricketRules;import rules.FootballRules;import rules.KabaddiRules;import rules.SportRules;import service.AdminConsoleService;import service.AuthService;import util.PasswordHasher;import java.nio.file.Path;import java.sql.SQLException;import java.time.LocalDate;import java.time.LocalTime;import java.util.ArrayList;import java.util.List;import java.util.Scanner;

public class SportifyApp {
    private final Scanner scanner = new Scanner(System.in);
    private final AdminConsoleService consoleService;

    private SportifyApp(SportRules rules) { consoleService = new AdminConsoleService(rules); }

    public static void main(String[] args) {
        Sport sport;
        try { sport = Sport.valueOf(System.getProperty("sportify.sport", "FOOTBALL").toUpperCase()); }
        catch (IllegalArgumentException exception) { System.out.println("Sport must be FOOTBALL, CRICKET, or KABADDI."); return; }
        SportRules rules = switch (sport) {
            case FOOTBALL -> new FootballRules();
            case CRICKET -> new CricketRules();
            case KABADDI -> new KabaddiRules();
        };
        SportifyApp app = new SportifyApp(rules);
        try {
            if (app.login()) app.showMenu();
        } catch (SQLException exception) {
            System.out.println("Database is unavailable: " + exception.getMessage());
        }
    }

    private boolean login() throws SQLException {
        AuthService authService = new AuthService(new AdminDAO(), new PasswordHasher());
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Admin username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Admin password: ");
            char[] password = scanner.nextLine().toCharArray();
            boolean accepted;
            try { accepted = authService.login(username, password); }
            finally { java.util.Arrays.fill(password, '\0'); }
            if (accepted) {
                System.out.println("Login successful.");
                return true;
            }
            System.out.println("Invalid username or password. Attempts remaining: " + authService.getRemainingAttempts());
        }
        System.out.println("Login locked after three failed attempts.");
        return false;
    }

    private void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n1. Manage Teams  \n2. Manage Players \n3. Manage Venues \n4. Schedule Match");
            System.out.println("5. Enter Match Result  \n6. View Points Table \n7. View Top Performers");
            System.out.println("8. Search Player \n9. Team Match History \n10. Upcoming Fixtures");
            System.out.println("11. Import Players from CSV \n12. Export Reports to CSV \n13. Process Pending Results \n0. Exit");
            Integer choice = readInteger("Choice: ");
            if (choice == null) continue;
            try {
                switch (choice) {
                    case 1 -> manageTeams();
                    case 2 -> managePlayers();
                    case 3 -> manageVenues();
                    case 4 -> scheduleMatch();
                    case 5 -> enterResult();
                    case 6 -> printRows(consoleService.pointsTable());
                    case 7 -> printRows(consoleService.leaderboard());
                    case 8 -> printPlayers(consoleService.searchPlayers(readText("Player name or team: ")));
                    case 9 -> printRows(consoleService.teamHistory(readText("Team ID: ")));
                    case 10 -> printRows(consoleService.upcoming());
                    case 11 -> importPlayers();
                    case 12 -> { consoleService.exportAll(Path.of("reports")); System.out.println("Reports saved in reports folder."); }
                    case 13 -> System.out.println("Processed " + consoleService.processPendingResults() + " pending result(s).");
                    case 0 -> running = false;
                    default -> System.out.println("Choose a menu number from 0 to 13.");
                }
            } catch (SQLException | java.io.IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
                System.out.println("Could not complete that action: " + exception.getMessage());
            } catch (RuntimeException exception) {
                System.out.println("Could not complete that action: " + exception.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    private void manageTeams() throws SQLException {
        int operation = askInt("1 Add/update  2 Delete  3 List: ");
        if (operation == 1) {
            String teamId = readText("Team ID: ");
            Team team = new Team(teamId, readText("Name: "), readText("City: "), readText("Coach: "));
            consoleService.saveTeam(team);
            String captainId = readText("Captain player ID (blank to assign later): ");
            if (!captainId.isBlank()) consoleService.assignCaptain(teamId, captainId);
            System.out.println("Team saved.");
        } else if (operation == 2) {
            consoleService.deleteTeam(readText("Team ID: "));
            System.out.println("Team deleted.");
        } else if (operation == 3) {
            for (Team team : consoleService.teams()) System.out.println(team.getTeamId() + " | " + team.getName() + " | " + team.getCity() + " | " + team.getCoach() + " | captain: " + (team.getCaptain() == null ? "not assigned" : team.getCaptain().getName()));
        } else System.out.println("Unknown team action.");
    }

    private void managePlayers() throws SQLException {
        int operation = askInt("1 Add/update  2 Delete  3 List  4 Transfer player: ");
        if (operation == 1) {
            String id = readText("Player ID: "); String name = readText("Name: "); int age = askInt("Age: ");
            PlayerRole role = PlayerRole.valueOf(readText("Role (" + java.util.Arrays.toString(PlayerRole.values()) + "): ").toUpperCase());
            int jersey = askInt("Jersey number: "); String teamId = readText("Team ID: ");
            consoleService.savePlayer(new Player(id, name, age, role, jersey, teamId));
            System.out.println("Player saved.");
        } else if (operation == 2) {
            consoleService.deletePlayer(readText("Player ID: ")); System.out.println("Player deleted.");
        } else if (operation == 3) printPlayers(consoleService.players());
        else if (operation == 4) {
            Player oldPlayer = consoleService.findPlayer(readText("Player ID: "));
            String teamId = readText("Destination team ID: ");
            consoleService.savePlayer(new Player(oldPlayer.getPlayerId(), oldPlayer.getName(), oldPlayer.getAge(), oldPlayer.getRole(), oldPlayer.getJerseyNumber(), teamId));
            System.out.println("Player transferred.");
        }
        else System.out.println("Unknown player action.");
    }

    private void manageVenues() throws SQLException {
        int operation = askInt("1 Add/update  2 List: ");
        if (operation == 1) {
            consoleService.saveVenue(new Venue(readText("Venue ID: "), readText("Name: "), readText("City: "), askInt("Capacity: ")));
            System.out.println("Venue saved.");
        } else if (operation == 2) {
            for (Venue venue : consoleService.venues()) System.out.println(venue.getVenueId() + " | " + venue.getName() + " | " + venue.getCity() + " | " + venue.getCapacity());
        } else System.out.println("Unknown venue action.");
    }

    private void scheduleMatch() throws SQLException {
        String matchId = readText("Match ID: "); String home = readText("Home team ID: "); String away = readText("Away team ID: "); String venue = readText("Venue ID: ");
        LocalDate date = LocalDate.parse(readText("Date (YYYY-MM-DD): ")); LocalTime time = LocalTime.parse(readText("Time (HH:MM): "));
        consoleService.schedule(new Match(matchId, home, away, venue, date, time));
        System.out.println("Match scheduled.");
    }

    private void enterResult() throws InterruptedException {
        String matchId = readText("Match ID: "); int homeScore = askInt("Home score: "); int awayScore = askInt("Away score: ");
        int statCount = askInt("Number of player stat rows: ");
        List<String> playerIds = new ArrayList<>(); List<Integer> stats = new ArrayList<>();
        for (int i = 0; i < statCount; i++) { playerIds.add(readText("Player ID: ")); stats.add(askInt("" + (i + 1) + " " + "Stat contribution: ")); }
        List<MatchStat> matchStats = consoleService.readStats(matchId, playerIds, stats);
        consoleService.enqueueResult(matchId, homeScore, awayScore, matchStats);
        System.out.println("Result queued. Choose option 13 to process pending results.");
    }

    private void importPlayers() throws SQLException, java.io.IOException {
        var report = consoleService.importPlayers(Path.of(readText("CSV file path: ")));
        System.out.println("Imported: " + report.getImported() + ", rejected: " + report.getRejected() + ", skipped: " + report.getSkipped());
        printRows(report.getDetails());
    }

    private String readText(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    private Integer readInteger(String prompt) {
        System.out.print(prompt);
        try { return Integer.valueOf(scanner.nextLine().trim()); }
        catch (NumberFormatException exception) { System.out.println("Enter a whole number."); return null; }
    }
    private int askInt(String prompt) { Integer value = readInteger(prompt); if (value == null) throw new InvalidInputException("A whole number is required."); return value; }
    private void printRows(List<String> rows) { for (String row : rows) System.out.println(row); if (rows.isEmpty()) System.out.println("No records found."); }
    private void printPlayers(List<Player> players) { for (Player player : players) System.out.println(player.getDisplayInfo()); if (players.isEmpty()) System.out.println("No players found."); }
}
