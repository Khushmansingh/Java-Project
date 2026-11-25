package main;

import exception.*;
import model.*;
import service.PlayerService;
import service.PointsTableService;
import service.TeamService;
import service.ResultService;
import rules.FootballRules;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


public class CollectionsDemo {
    public static void main(String[] args) {
        TeamService teamService = new TeamService();
        PlayerService playerService = new PlayerService();
        PointsTableService pointsTableService = new PointsTableService();

        Team mumbai = new Team("T01", "Mumbai Strikers", "Mumbai", "Coach A");
        Team delhi = new Team("T02", "Delhi Dynamos", "Delhi", "Coach B");
        teamService.addTeam(mumbai);
        teamService.addTeam(delhi);
        playerService.addPlayer(new Player("P01", "Asha Mehta", 22, PlayerRole.FORWARD, 10, "T01"), mumbai);
        playerService.addPlayer(new Player("P02", "Ravi Shah", 24, PlayerRole.MIDFIELDER, 8, "T01"), mumbai);
        playerService.addPlayer(new Player("P03", "Neha Rao", 20, PlayerRole.FORWARD, 9, "T02"), delhi);

        Map<String, Integer> goals = new HashMap<>();
        goals.put("P01", 4);
        goals.put("P02", 2);
        goals.put("P03", 3);
        if (!playerService.topScorers(goals).get(0).getPlayerId().equals("P01")) {
            throw new AssertionError("Top scorers were not sorted by stat.");
        }
        if (playerService.findByTeamAndRole("T01", PlayerRole.FORWARD).size() != 1) {
            throw new AssertionError("Team and role filter returned the wrong players.");
        }
        if (!playerService.playerNamesByTeamAndRole("T01", PlayerRole.FORWARD).contains("Asha Mehta")) {
            throw new AssertionError("Stream name mapping returned the wrong player.");
        }

        boolean duplicateRejected = false;
        try {
            teamService.addTeam(new Team("T03", "mumbai strikers", "Pune", "Coach C"));
        } catch (DuplicateRecordException exception) {
            duplicateRejected = true;
        }
        if (!duplicateRejected) throw new AssertionError("Duplicate team name was accepted.");

        boolean jerseyRejected = false;
        try { playerService.addPlayer(new Player("P04", "Duplicate Jersey", 21, PlayerRole.DEFENDER, 10, "T01"), mumbai); }
        catch (DuplicateRecordException exception) { jerseyRejected = true; }
        if (!jerseyRejected) throw new AssertionError("Duplicate jersey was accepted.");
        boolean ageRejected = false;
        try { new Player("P05", "Too Young", 13, PlayerRole.FORWARD, 20, "T01"); }
        catch (InvalidInputException exception) { ageRejected = true; }
        if (!ageRejected) throw new AssertionError("Out-of-range age was accepted.");
        boolean missingIdHandled = false;
        try { playerService.findById("UNKNOWN"); }
        catch (InvalidInputException exception) { missingIdHandled = true; }
        if (!missingIdHandled) throw new AssertionError("Unknown player ID was not handled.");
        boolean selfMatchRejected = false;
        try { new Match("BAD", "T01", "T01", "V01", LocalDate.of(2026, 12, 5), LocalTime.NOON); }
        catch (InvalidInputException exception) { selfMatchRejected = true; }
        if (!selfMatchRejected) throw new AssertionError("A team was allowed to play itself.");

        teamService.updateTeam("T02", "Delhi Dynamos FC", "Delhi", "Coach B");
        PointsEntry mumbaiPoints = pointsTableService.getOrCreate("T01");
        mumbaiPoints.recordResult(true, false, 3, 2);
        pointsTableService.getOrCreate("T02").recordResult(false, false, 0, -2);
        Map<String, String> teamNames = new HashMap<>();
        teamNames.put("T01", mumbai.getName());
        teamNames.put("T02", teamService.findById("T02").getName());
        List<PointsEntry> table = pointsTableService.sortedTable(teamNames);
        if (!table.get(0).getTeamId().equals("T01")) throw new AssertionError("Points table ordering failed.");

        boolean protectedDelete = false;
        try {
            teamService.removeTeam("T01", playerService.findAll(), java.util.Collections.emptySet());
        } catch (RuntimeException exception) {
            protectedDelete = true;
        }
        if (!protectedDelete) throw new AssertionError("Team with players was deleted.");
        if (!playerService.removePlayer("P03")) throw new AssertionError("Safe player removal failed.");

        PointsTableService drawPoints = new PointsTableService();
        Team chennai = new Team("T03", "Chennai Kings", "Chennai", "Coach C");
        Team punjab = new Team("T04", "Punjab Lions", "Chandigarh", "Coach D");
        teamService.addTeam(chennai); teamService.addTeam(punjab);
        Player chennaiPlayer = new Player("P30", "Chennai Player", 23, PlayerRole.FORWARD, 10, "T03");
        playerService.addPlayer(chennaiPlayer, chennai);
        ResultService results = new ResultService(new FootballRules(), teamService, playerService, drawPoints);
        results.addMatch(new Match("M102", "T03", "T04", "V01", LocalDate.of(2026, 12, 6), LocalTime.NOON));
        results.recordResult("M102", 0, 0, List.of(new MatchStat("M102", "P30", 0, 0)));
        if (drawPoints.getOrCreate("T03").getPoints() != 1 || drawPoints.getOrCreate("T04").getPoints() != 1) throw new AssertionError("Football draw did not award one point to each team.");
        boolean duplicateResultRejected = false;
        try { results.recordResult("M102", 1, 0); }
        catch (InvalidResultException exception) { duplicateResultRejected = true; }
        if (!duplicateResultRejected) throw new AssertionError("A second result was accepted.");

        PointsTableService tieTable = new PointsTableService();
        tieTable.getOrCreate("T05").recordResult(true, false, 3, 1);
        tieTable.getOrCreate("T06").recordResult(true, false, 3, 1);
        tieTable.getOrCreate("T07").recordResult(true, false, 3, 2);
        Map<String, String> tieNames = new HashMap<>(); tieNames.put("T05", "Zulu"); tieNames.put("T06", "Alpha"); tieNames.put("T07", "Beta");
        List<PointsEntry> ordered = tieTable.sortedTable(tieNames);
        if (!ordered.get(0).getTeamId().equals("T07")) throw new AssertionError("Score difference tie-break failed.");
        if (!ordered.get(1).getTeamId().equals("T06")) throw new AssertionError("Team name tie-break failed.");

        System.out.println("Collection and rule checks passed: CRUD, uniqueness, validation, filters, results, draw points, ordering, safe removal.");
    }
}
