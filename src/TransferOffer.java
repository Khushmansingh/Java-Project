import java.time.LocalDate;
public class TransferOffer implements Eligible
{
    private int offerId, teamId;
    private double offeredSalary, offerAmount, minRating;
    private LocalDate deadline;
    private boolean open;
    public TransferOffer(int offerId, int teamId, double offeredSalary, double offerAmount, LocalDate deadline, double minRating, boolean open)
    {
        this.offerId=offerId;
        this.teamId=teamId;
        this.offeredSalary=offeredSalary;
        this.offerAmount=offerAmount;
        this.deadline=deadline;
        this.minRating=minRating;
        this.open=open;
    }
    public int getOfferId()
    {
        return offerId;
    }
    public int getTeamId()
    {
        return teamId;
    }
    public double getOfferedSalary()
    {
        return offeredSalary;
    }
    public double getOfferAmount()
    {
        return offerAmount;
    }
    public LocalDate getDeadline()
    {
        return deadline;
    }
    public double getMinRating()
    {
        return minRating;
    }
    public boolean isOpen()
    {
        return open;
    }
    public boolean isEligible(Driver driver)
    {
        return driver.getRating() >= minRating;
    }
    public String toString()
    {
        return offerId + " | team=" + teamId + " | min rating=" + minRating + " | salary=" + offeredSalary + " | deadline=" + deadline + " | open=" + open;
    }
}
