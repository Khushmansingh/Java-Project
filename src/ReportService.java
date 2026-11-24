import java.util.*;
import java.util.stream.Collectors;
public class ReportService
{
    private Database db;
    public ReportService(Database db)
    {
        this.db=db;
    }
    public String buildReport() throws Exception
    {
        List<Driver> drivers=db.drivers();
        List<TransferRequest> requests=db.requests();
        StringBuilder b=new StringBuilder();
        b.append("Drivers: ").append(drivers.size()).append("\nRequests: ").append(requests.size()).append("\n");
        b.append("Average rating: ").append(drivers.stream().mapToDouble(Driver::getRating).average().orElse(0)).append("\n");
        b.append("Requests by status: ").append(requests.stream().collect(Collectors.groupingBy(TransferRequest::getStatus,Collectors.counting()))).append("\n");
        b.append("Top drivers:\n");
        drivers.stream().sorted(Comparator.comparingDouble(Driver::getRating).reversed().thenComparing(Driver::getName)).limit(5).forEach(d->b.append(d).append("\n"));
        b.append("Nationalities: ").append(drivers.stream().map(Driver::getNationality).distinct().collect(Collectors.toList())).append("\n");
        b.append("Drivers by nationality: ").append(drivers.stream().collect(Collectors.groupingBy(Driver::getNationality,Collectors.counting()))).append("\n");
        b.append("League standings:\n");
        for(Map.Entry<Integer,Integer> e:db.standings().entrySet())
        {
            Driver d=db.findDriver(e.getKey());
            b.append((d==null?"Driver "+e.getKey():d.getName())+" | "+e.getValue()+" points\n");
        }
        b.append("Team finances:\n");
        for(Team t:db.teams())b.append(t.getTeamName()+" budget="+t.getBudget()+" seats="+t.getMaxSeats()+"\n");
        return b.toString();
    }
}
