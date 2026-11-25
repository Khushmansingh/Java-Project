package main;

import concurrency.ResultConsumer;
import concurrency.ResultProducer;
import concurrency.ResultQueue;
import concurrency.ResultTask;
import model.Match;
import model.PointsEntry;
import model.Team;
import model.Venue;
import rules.FootballRules;
import service.PlayerService;
import service.PointsTableService;
import service.ResultService;
import service.TeamService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class QueueAndExecutorDemo {
    public static void main(String[] args) throws Exception {
        TeamService teams = new TeamService();
        Team homeTeam = new Team("T01", "Mumbai Strikers", "Mumbai", "Coach A");
        Team awayTeam = new Team("T02", "Delhi Dynamos", "Delhi", "Coach B");
        teams.addTeam(homeTeam);
        teams.addTeam(awayTeam);
        PointsTableService points = new PointsTableService();
        ResultService resultService = new ResultService(new FootballRules(), teams, new PlayerService(), points);
        resultService.addMatch(new Match("M101", "T01", "T02", "V01", LocalDate.of(2026, 12, 1), LocalTime.of(18, 0)));

        ResultQueue queue = new ResultQueue();
        Thread consumer = new Thread(new ResultConsumer(queue), "ResultConsumer");
        consumer.start();
        new ResultProducer(queue).submit(new ResultTask("M101", 2, 1, resultService));
        queue.close();
        consumer.join();
        if (resultService.findMatch("M101").getStatus() != model.MatchStatus.COMPLETED) {
            throw new AssertionError("Consumer did not process queued result.");
        }
        System.out.println("Queue demo passed: result was consumed and points were updated.");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> completedMatchCount = () -> {
                int count = 0;
                for (var match : resultService.getMatches().values()) {
                    if (match.getStatus() == model.MatchStatus.COMPLETED) count++;
                }
                return count;
            };
            Future<Integer> result = executor.submit(completedMatchCount);
            if (result.get() != 1) throw new AssertionError("Callable returned an incorrect completed-match count.");
            System.out.println("Executor demo passed: completed matches = " + result.get());
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow();
        }
    }
}
