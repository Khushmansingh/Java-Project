package main;

import dao.PlayerDAO;
import service.PlayerService;
import service.TeamService;
import util.CsvImporter;
import util.CsvImportReport;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvValidationDemo {
    public static void main(String[] args) throws Exception {
        Path csv = Files.createTempFile("sportify-invalid-row", ".csv");
        try {
            Files.writeString(csv, "player_id,name,age,role,jersey_no,team_id\nBAD,ROW\n");
            CsvImportReport report = new CsvImporter(new PlayerService(), new TeamService(), new PlayerDAO()).importPlayers(csv);
            if (report.getRejected() != 1 || !report.getDetails().get(0).startsWith("Row 2:")) {
                throw new AssertionError("Malformed row was not reported with its row number.");
            }
            System.out.println("CSV validation demo passed: " + report.getDetails().get(0));
        } finally {
            Files.deleteIfExists(csv);
        }
    }
}
