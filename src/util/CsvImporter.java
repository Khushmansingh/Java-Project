package util;

import dao.PlayerDAO;
import exception.DuplicateRecordException;
import exception.InvalidInputException;
import model.Player;
import model.PlayerRole;
import model.Team;
import service.PlayerService;
import service.TeamService;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class CsvImporter {
    private final PlayerService playerService;
    private final TeamService teamService;
    private final PlayerDAO playerDAO;
    public CsvImporter(PlayerService playerService, TeamService teamService, PlayerDAO playerDAO) { this.playerService = playerService; this.teamService = teamService; this.playerDAO = playerDAO; }

    public CsvImportReport importPlayers(Path path) throws IOException {
        CsvImportReport report = new CsvImportReport();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (rowNumber == 1 && line.toLowerCase().startsWith("player_id,")) continue;
                if (line.isBlank()) { report.skipped(rowNumber); continue; }
                try {
                    String[] columns = splitCsv(line);
                    if (columns.length != 6) throw new InvalidInputException("Expected 6 columns.");
                    String playerId = columns[0].trim();
                    String name = columns[1].trim();
                    int age = Integer.parseInt(columns[2].trim());
                    PlayerRole role = PlayerRole.valueOf(columns[3].trim().toUpperCase());
                    int jersey = Integer.parseInt(columns[4].trim());
                    String teamId = columns[5].trim();
                    Team team = teamService.findById(teamId);
                    Player player = new Player(playerId, name, age, role, jersey, teamId);
                    playerService.addPlayer(player, team);
                    try { playerDAO.save(player); }
                    catch (SQLException exception) { playerService.removePlayer(playerId); throw new InvalidInputException("Database save failed: " + exception.getMessage()); }
                    report.imported();
                } catch (IllegalArgumentException | InvalidInputException | DuplicateRecordException exception) {
                    report.rejected(rowNumber, exception.getMessage());
                }
            }
        }
        return report;
    }

    private String[] splitCsv(String line) {
        java.util.List<String> values = new java.util.ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"' && quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
            else if (current == '"') quoted = !quoted;
            else if (current == ',' && !quoted) { values.add(value.toString()); value.setLength(0); }
            else value.append(current);
        }
        if (quoted) throw new InvalidInputException("Unclosed quoted value.");
        values.add(value.toString());
        return values.toArray(new String[0]);
    }
}
