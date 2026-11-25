package rules;

public class CricketRules implements SportRules {
    public int pointsForWin() { return 2; }
    public int pointsForDrawOrTie() { return 1; }
    public int pointsForLoss() { return 0; }
    public String getStatLabel() { return "Runs"; }
}
