package main;

import exception.InvalidInputException;
import model.Player;
import model.PlayerRole;
import model.Team;
import rules.FootballRules;

public class ModelDemo {
    public static void main(String[] args) {
        try {
            Integer jerseyNumber = Integer.valueOf(args.length > 0 ? args[0] : "10");
            Team team = new Team("T01", "Mumbai Strikers", "Mumbai", "A. Coach");
            Player captain = new Player("P01", "Asha Mehta", 22, PlayerRole.FORWARD, jerseyNumber, "T01");
            team.appointCaptain(captain);
            StringBuilder report = new StringBuilder();
            report.append("Team: ").append(team.getName()).append('\n');
            report.append("Captain: ").append(team.getCaptain().getDisplayInfo()).append('\n');
            report.append("Sport stat: ").append(new FootballRules().getStatLabel()).append('\n');
            report.append("Points for a win: ").append(new FootballRules().pointsForWin());
            System.out.println(report);
        } catch (NumberFormatException exception) {
            System.out.println("Jersey number must be a whole number.");
        } catch (InvalidInputException exception) {
            System.out.println("Cannot create the example: " + exception.getMessage());
        }
    }
}
