import java.util.*;
import java.util.regex.Pattern;
public class DriverService
{
    private Database db;
    public DriverService(Database db)
    {
        this.db=db;
    }
    public void validate(Driver d) throws InvalidDriverException
    {
        if(d.getName()==null||d.getName().trim().isEmpty()||d.getNationality()==null||d.getNationality().trim().isEmpty()||d.getEmail()==null||d.getEmail().trim().isEmpty()) throw new InvalidDriverException("Name, nationality and email are required");
        if(d.getDriverId()<=0) throw new InvalidDriverException("Driver ID must be positive");
        if(d.getRating()<0||d.getRating()>100) throw new InvalidDriverException("Rating must be from 0 to 100");
        if(d.getSalary()<0||d.getMarketValue()<0) throw new InvalidDriverException("Salary and market value cannot be negative");
        if(!Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",d.getEmail())) throw new InvalidDriverException("Email format is invalid");
        if(!Arrays.asList("FIT","INJURED","SUSPENDED").contains(d.getStatus())) throw new InvalidDriverException("Status must be FIT, INJURED or SUSPENDED");
    }
    public void register(Driver d) throws Exception
    {
        validate(d);
        HashMap<Integer,Driver> driverMap=new HashMap<>();
        HashSet<String> emailSet=new HashSet<>();
        for(Driver old:db.drivers())
        {
            driverMap.put(old.getDriverId(),old);
            emailSet.add(old.getEmail().toLowerCase(Locale.ROOT));
        }
        if(driverMap.containsKey(d.getDriverId()))throw new DuplicateRecordException("Driver ID already exists");
        if(emailSet.contains(d.getEmail().toLowerCase(Locale.ROOT)))throw new DuplicateRecordException("Email already exists");
        db.saveDriver(d);
    }
    public Driver search(int id) throws Exception
    {
        return db.findDriver(id);
    }
    public void update(Driver d) throws Exception
    {
        validate(d);
        for(Driver old:db.drivers())if(old.getDriverId()!=d.getDriverId()&&old.getEmail().equalsIgnoreCase(d.getEmail()))throw new DuplicateRecordException("Email already exists");
        if(db.findDriver(d.getDriverId())==null)throw new InvalidDriverException("Driver not found");
        db.updateDriver(d);
    }
    public void removeFromList(List<Driver> driverList,int driverId)
    {
        Iterator<Driver> iterator=driverList.iterator();
        while(iterator.hasNext())if(iterator.next().getDriverId()==driverId)iterator.remove();
    }
}
