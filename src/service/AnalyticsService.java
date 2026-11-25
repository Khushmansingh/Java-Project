package service;

import model.Match;
import model.MatchStatus;
import model.MatchStat;
import model.Player;
import model.Team;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {
    public Map<MatchStatus, Long> countMatchesByStatus(List<Match> matches) {
        // filter and map are intermediate operations; collect is the terminal operation.
        return matches.stream().collect(Collectors.groupingBy(Match::getStatus, Collectors.counting()));
    }

    public Map<String, Long> countWinsByTeam(List<Match> matches) {
        return matches.stream().filter(match -> match.getStatus() == MatchStatus.COMPLETED && match.getWinnerId() != null)
                .collect(Collectors.groupingBy(Match::getWinnerId, Collectors.counting()));
    }

    public double averageScorePerMatch(List<Match> matches) {
        return matches.stream().filter(match -> match.getStatus() == MatchStatus.COMPLETED)
                .collect(Collectors.averagingDouble(match -> match.getHomeScore() + match.getAwayScore()));
    }

    public Map<String, Double> averageStatsByPlayer(List<Player> players, List<MatchStat> stats) {
        Map<String, List<MatchStat>> statsByPlayer = stats.stream().collect(Collectors.groupingBy(MatchStat::getPlayerId));
        return players.stream().collect(Collectors.toMap(Player::getPlayerId,
                player -> statsByPlayer.getOrDefault(player.getPlayerId(), List.of()).stream()
                        .collect(Collectors.averagingDouble(MatchStat::getScoreContribution))));
    }

    public java.util.DoubleSummaryStatistics summarizeMatchScores(List<Match> matches) {
        return matches.stream().filter(match -> match.getStatus() == MatchStatus.COMPLETED)
                .collect(Collectors.summarizingDouble(match -> match.getHomeScore() + match.getAwayScore()));
    }

    public List<String> distinctCities(List<Team> teams) {
        return teams.stream().map(Team::getCity).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
