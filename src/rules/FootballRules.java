package rules;

public class FootballRules implements SportRules {
    public int pointsForWin() { return 3; }
    public int pointsForDrawOrTie() { return 1; }
    public int pointsForLoss() { return 0; }
    public String getStatLabel() { return "Goals"; }
}
