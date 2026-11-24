public class SlotManager
{
    private int freeSlots;
    public SlotManager(int freeSlots)
    {
        this.freeSlots=freeSlots;
    }
    public synchronized boolean reserve()
    {
        if(freeSlots<=0)return false;
        freeSlots--;
        return true;
    }
}
