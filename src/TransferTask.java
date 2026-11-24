import java.util.*;
import java.util.concurrent.*;
public class TransferTask implements Runnable
{
    private SlotManager slots;
    public TransferTask(SlotManager slots)
    {
        this.slots=slots;
    }
    public void run()
    {
        try
        {
            Thread.sleep(40);
            boolean ok=slots.reserve();
            System.out.println(Thread.currentThread().getName()+" reservation: "+(ok?"succeeded":"no seat"));
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
            System.out.println(Thread.currentThread().getName()+" interrupted");
        }
    }
    public static class RequestQueue
    {
        private Queue<TransferRequest> queue=new LinkedList<>();
        public synchronized void put(TransferRequest r)
        {
            queue.add(r);
            notifyAll();
        }
        public synchronized TransferRequest take() throws InterruptedException
        {
            while(queue.isEmpty())wait();
            return queue.remove();
        }
    }
    public static void processPending(List<TransferRequest> requests) throws Exception
    {
        RequestQueue q=new RequestQueue();
        ExecutorService pool=Executors.newFixedThreadPool(2);
        pool.submit(()->
        {
            for(TransferRequest r:requests)q.put(r);q.put(new TransferRequest(-1,-1,-1,"END"));
        }
        );
        Future<Integer> result=pool.submit(()->
        {
            int count=0;while(true)
            {
                TransferRequest r=q.take();if(r.getRequestId()==-1)break;System.out.println("Consumer processed pending request "+r.getRequestId());count++;
            }
            return count;
        }
        );
        System.out.println("Processed count: "+result.get());
        pool.shutdown();
        pool.awaitTermination(5,TimeUnit.SECONDS);
    }
}
