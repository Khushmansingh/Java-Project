package concurrency;

import java.util.LinkedList;
import java.util.Queue;

public class ResultQueue {
    private final Queue<ResultTask> tasks = new LinkedList<>();
    private boolean closed;

    public synchronized void add(ResultTask task) throws InterruptedException {
        while (tasks.size() >= 20 && !closed) wait();
        if (closed) throw new IllegalStateException("Result queue is closed.");
        tasks.add(task);
        notifyAll();
    }

    public synchronized ResultTask take() throws InterruptedException {
        while (tasks.isEmpty() && !closed) wait();
        ResultTask task = tasks.poll();
        notifyAll();
        return task;
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    public synchronized int size() { return tasks.size(); }
}
