package rules;

public interface SportRules {
    int pointsForWin();
    int pointsForDrawOrTie();
    int pointsForLoss();
    String getStatLabel();
}
