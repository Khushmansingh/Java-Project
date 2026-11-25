package model;

import exception.InvalidInputException;

public class MatchStat {
    private final String matchId;
    private final String playerId;
    private final int scoreContribution;
    private final int secondaryContribution;

    public MatchStat(String matchId, String playerId, int scoreContribution, int secondaryContribution) {
        this.matchId = Person.requireText(matchId, "Match ID");
        this.playerId = Person.requireText(playerId, "Player ID");
        if (scoreContribution < 0 || secondaryContribution < 0) throw new InvalidInputException("Player stats cannot be negative.");
        this.scoreContribution = scoreContribution;
        this.secondaryContribution = secondaryContribution;
    }

    public String getMatchId() { return matchId; }
    public String getPlayerId() { return playerId; }
    public int getScoreContribution() { return scoreContribution; }
    public int getSecondaryContribution() { return secondaryContribution; }
}
