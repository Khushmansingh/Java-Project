package concurrency;

import service.ResultService;

public class ResultTask {
    private final String matchId;
    private final int homeScore;
    private final int awayScore;
    private final ResultService resultService;
    private final Runnable databaseAction;

    public ResultTask(String matchId, int homeScore, int awayScore, ResultService resultService) {
        this.matchId = matchId;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.resultService = resultService;
        this.databaseAction = null;
    }

    public ResultTask(Runnable databaseAction) {
        this.matchId = null;
        this.homeScore = 0;
        this.awayScore = 0;
        this.resultService = null;
        this.databaseAction = databaseAction;
    }

    public void process() {
        if (databaseAction != null) databaseAction.run();
        else resultService.recordResult(matchId, homeScore, awayScore);
    }
}
