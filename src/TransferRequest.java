public class TransferRequest
{
    private int requestId, driverId, offerId;
    private String status;
    public TransferRequest(int requestId, int driverId, int offerId, String status)
    {
        this.requestId=requestId;
        this.driverId=driverId;
        this.offerId=offerId;
        this.status=status;
    }
    public int getRequestId()
    {
        return requestId;
    }
    public int getDriverId()
    {
        return driverId;
    }
    public int getOfferId()
    {
        return offerId;
    }
    public String getStatus()
    {
        return status;
    }
    public String toString()
    {
        return requestId + " | driver=" + driverId + " | offer=" + offerId + " | " + status;
    }
}
