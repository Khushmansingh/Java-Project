package concurrency;

public class ResultConsumer implements Runnable {
    private final ResultQueue queue;
    public ResultConsumer(ResultQueue queue) { this.queue = queue; }

    @Override
    public void run() {
        try {
            ResultTask task;
            while ((task = queue.take()) != null) task.process();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
