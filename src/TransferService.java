import java.time.LocalDate;
public class TransferService
{
    private Database db;
    public TransferService(Database db)
    {
        this.db=db;
    }
    public void checkEligibility(int driverId,int offerId) throws Exception
    {
        Driver d=db.findDriver(driverId);
        TransferOffer o=db.findOffer(offerId);
        if(d==null||o==null)
        {
            System.out.println("Driver or offer not found");
            return;
        }
        System.out.println(o.isEligible(d)?"ELIGIBLE":"NOT ELIGIBLE: rating is below minimum");
    }
    public void submit(int driverId,int offerId) throws Exception
    {
        Driver d=db.findDriver(driverId);
        TransferOffer o=db.findOffer(offerId);
        if(d==null||o==null)throw new InvalidDriverException("Driver or offer not found");
        if(!o.isEligible(d))throw new InvalidDriverException("Driver rating is below offer minimum");
        if(!d.getStatus().equals("FIT"))throw new InvalidDriverException("Driver is not fit");
        if(!o.isOpen()||LocalDate.now().isAfter(o.getDeadline()))throw new TransferClosedException("Offer is closed or past deadline");
        Team t=db.findTeam(o.getTeamId());
        if(t==null)throw new InvalidDriverException("Team not found");
        long occupied=db.drivers().stream().filter(x->x.getTeamId()!=null&&x.getTeamId()==t.getTeamId()).count();
        if(occupied>=t.getMaxSeats())throw new InvalidDriverException("Team has no free seat");
        if(t.getBudget()<o.getOfferedSalary()+o.getOfferAmount())throw new InvalidDriverException("Team budget is insufficient");
        for(TransferRequest r:db.requests())if(r.getDriverId()==driverId&&r.getOfferId()==offerId)throw new DuplicateRecordException("Driver already requested this offer");
        db.saveRequest(driverId,offerId);
    }
    public void setStatus(int requestId,String status) throws Exception
    {
        TransferRequest found=null;
        for(TransferRequest r:db.requests())if(r.getRequestId()==requestId)found=r;
        if(found==null)throw new InvalidDriverException("Request not found");
        boolean allowed=(found.getStatus().equals("PENDING")&&(status.equals("SHORTLISTED")||status.equals("REJECTED")))||(found.getStatus().equals("SHORTLISTED")&&(status.equals("SIGNED")||status.equals("REJECTED")));
        if(!allowed)throw new InvalidDriverException("Transition is not allowed");
        db.updateRequestStatus(requestId,found.getDriverId(),found.getOfferId(),status);
    }
}
