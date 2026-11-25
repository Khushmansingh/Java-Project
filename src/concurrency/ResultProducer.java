package concurrency;

public class ResultProducer {
    private final ResultQueue queue;
    public ResultProducer(ResultQueue queue) { this.queue = queue; }
    public void submit(ResultTask task) throws InterruptedException { queue.add(task); }
}
