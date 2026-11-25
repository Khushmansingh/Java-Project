package main;

import concurrency.VenueSlotManager;
import model.PointsEntry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyDemo {
    public static void main(String[] args) throws InterruptedException {
        VenueSlotManager manager = new VenueSlotManager();
        AtomicInteger winners = new AtomicInteger();
        Thread[] bookingThreads = new Thread[5];
        for (int i = 0; i < bookingThreads.length; i++) {
            bookingThreads[i] = new Thread(() -> {
                if (manager.tryBook("V01", LocalDate.of(2026, 12, 1), LocalTime.of(18, 0))) {
                    winners.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " booked the slot.");
                } else {
                    System.out.println(Thread.currentThread().getName() + " could not book the slot.");
                }
            }, "Booker-" + (i + 1));
            bookingThreads[i].start();
        }
        for (Thread thread : bookingThreads) thread.join();
        if (winners.get() != 1) throw new AssertionError("Exactly one booking must succeed.");

        PointsEntry sharedEntry = new PointsEntry("T01");
        Thread[] scoreThreads = new Thread[5];
        for (int i = 0; i < scoreThreads.length; i++) {
            scoreThreads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) sharedEntry.recordResult(true, false, 3, 1);
            });
            scoreThreads[i].start();
        }
        for (Thread thread : scoreThreads) thread.join();
        if (sharedEntry.getPlayed() != 500 || sharedEntry.getPoints() != 1500) throw new AssertionError("Synchronized points update lost a result.");
        Thread sleeper = new Thread(() -> {
            try { Thread.sleep(5000); }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                System.out.println("Sleeper noticed interrupt and restored its interrupt flag.");
            }
        }, "InterruptDemo");
        sleeper.start();
        Thread.sleep(20);
        sleeper.interrupt();
        sleeper.join();
        System.out.println("Concurrency demo passed: 1 venue booking and " + sharedEntry.getPlayed() + " synchronized point updates.");
    }
}
