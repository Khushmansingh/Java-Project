package model;

import exception.InvalidInputException;

public class PointsEntry {
    private final String teamId;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int points;
    private int scoreDifference;

    public PointsEntry(String teamId) { this.teamId = Person.requireText(teamId, "Team ID"); }

    public synchronized void recordResult(boolean isWin, boolean isDraw, int earnedPoints, int scoreDifference) {
        if (isWin && isDraw) throw new InvalidInputException("A result cannot be both a win and a draw.");
        if (earnedPoints < 0) throw new InvalidInputException("Points cannot be negative.");
        played++;
        if (isWin) won++;
        else if (isDraw) drawn++;
        else lost++;
        points += earnedPoints;
        this.scoreDifference += scoreDifference;
    }

    public String getTeamId() { return teamId; }
    public synchronized int getPlayed() { return played; }
    public synchronized int getWon() { return won; }
    public synchronized int getDrawn() { return drawn; }
    public synchronized int getLost() { return lost; }
    public synchronized int getPoints() { return points; }
    public synchronized int getScoreDifference() { return scoreDifference; }
}
