package model;

import exception.InvalidInputException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Match {
    private final String matchId;
    private final String homeTeamId;
    private final String awayTeamId;
    private final String venueId;
    private final LocalDate date;
    private final LocalTime time;
    private MatchStatus status;
    private Integer homeScore;
    private Integer awayScore;
    private String winnerId;

    public Match(String matchId, String homeTeamId, String awayTeamId, String venueId, LocalDate date, LocalTime time) {
        this.matchId = Person.requireText(matchId, "Match ID");
        this.homeTeamId = Person.requireText(homeTeamId, "Home team ID");
        this.awayTeamId = Person.requireText(awayTeamId, "Away team ID");
        if (this.homeTeamId.equals(this.awayTeamId)) throw new InvalidInputException("A team cannot play itself.");
        this.venueId = Person.requireText(venueId, "Venue ID");
        if (date == null || time == null) throw new InvalidInputException("Match date and time are required.");
        this.date = date;
        this.time = time;
        this.status = MatchStatus.SCHEDULED;
    }

    public void complete(int homeScore, int awayScore) {
        if (status != MatchStatus.SCHEDULED) throw new InvalidInputException("Only a scheduled match can receive a result.");
        if (homeScore < 0 || awayScore < 0) throw new InvalidInputException("Scores cannot be negative.");
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.winnerId = homeScore == awayScore ? null : (homeScore > awayScore ? homeTeamId : awayTeamId);
        this.status = MatchStatus.COMPLETED;
    }

    public void cancel() {
        if (status != MatchStatus.SCHEDULED) throw new InvalidInputException("Only a scheduled match can be cancelled.");
        status = MatchStatus.CANCELLED;
    }

    public String getMatchId() { return matchId; }
    public String getHomeTeamId() { return homeTeamId; }
    public String getAwayTeamId() { return awayTeamId; }
    public String getVenueId() { return venueId; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public MatchStatus getStatus() { return status; }
    public Integer getHomeScore() { return homeScore; }
    public Integer getAwayScore() { return awayScore; }
    public String getWinnerId() { return winnerId; }
}
