package main;

import model.*;
import service.AnalyticsService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AnalyticsDemo {
    public static void main(String[] args) {
        Match match = new Match("M101", "T01", "T02", "V01", LocalDate.of(2026, 12, 1), LocalTime.of(18, 0));
        match.complete(2, 1);
        Player player = new Player("P01", "Asha Mehta", 22, PlayerRole.FORWARD, 10, "T01");
        AnalyticsService analytics = new AnalyticsService();
        List<Match> matches = List.of(match);
        System.out.println("Matches by status: " + analytics.countMatchesByStatus(matches));
        System.out.println("Wins by team: " + analytics.countWinsByTeam(matches));
        System.out.println("Average score per match: " + analytics.averageScorePerMatch(matches));
        System.out.println("Player average stat: " + analytics.averageStatsByPlayer(List.of(player), List.of(new MatchStat("M101", "P01", 2, 0))));
        System.out.println("Score summary: " + analytics.summarizeMatchScores(matches));
        System.out.println("Distinct team cities: " + analytics.distinctCities(List.of(new Team("T01", "Mumbai Strikers", "Mumbai", "Coach A"), new Team("T02", "Delhi Dynamos", "Delhi", "Coach B"))));
    }
}
