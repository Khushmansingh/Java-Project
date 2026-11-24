import java.time.LocalDate;
import java.util.*;
public class Main
{
    private static Scanner input=new Scanner(System.in);
    private static Database db=new Database();
    private static DriverService drivers=new DriverService(db);
    private static TransferService transfers=new TransferService(db);
    private static ReportService reports=new ReportService(db);
    private static String read(String label)
    {
        System.out.print(label);
        return input.nextLine().trim();
    }
    private static int number(String label)
    {
        return Integer.parseInt(read(label));
    }
    private static double decimal(String label)
    {
        return Double.parseDouble(read(label));
    }
    private static Driver readDriver(int id)
    {
        return new Driver(id,read("Name: "),read("Nationality: "),read("Email: "),decimal("Rating 0-100: "),decimal("Salary: "),decimal("Market value: "),null,read("Status FIT/INJURED/SUSPENDED: ").toUpperCase());
    }
    private static void driverMenu() throws Exception
    {
        System.out.println("1 Register  2 Search  3 Update  4 List");
        int c=number("Choice: ");
        if(c==1)
        {
            drivers.register(readDriver(number("Driver ID: ")));
            System.out.println("Driver registered");
        }
        else if(c==2)
        {
            Driver d=drivers.search(number("Driver ID: "));
            System.out.println(d==null?"Driver not found":d);
        }
        else if(c==3)
        {
            int id=number("Driver ID: ");
            if(drivers.search(id)==null)System.out.println("Driver not found");
            else
            {
                drivers.update(readDriver(id));
                System.out.println("Driver updated");
            }
        }
        else if(c==4)
        {
            for(Driver d:db.drivers())System.out.println(d);
        }
    }
    private static void teamMenu() throws Exception
    {
        System.out.println("1 Add team  2 List teams");
        int c=number("Choice: ");
        if(c==1)
        {
            int id=number("Team ID: ");
            String name=read("Team name: "),country=read("Country: ");
            double budget=decimal("Budget: ");
            Team t=new Team(id,name,country,budget,2);
            for(Team old:db.teams())if(old.getTeamId()==id)throw new DuplicateRecordException("Team ID already exists");
            db.saveTeam(t);
            System.out.println("Team added");
        }
        else if(c==2)for(Team t:db.teams())System.out.println(t);
    }
    private static void offerMenu() throws Exception
    {
        for(Team t:db.teams())System.out.println(t);
        int team=number("Team ID: ");
        if(db.findTeam(team)==null)throw new InvalidDriverException("Team not found");
        double salary=decimal("Offered salary: "),amount=decimal("Transfer amount: "),min=decimal("Minimum rating: ");
        LocalDate deadline=LocalDate.parse(read("Deadline YYYY-MM-DD: "));
        db.saveOffer(new TransferOffer(0,team,salary,amount,deadline,min,true));
        System.out.println("Offer created");
    }
    private static void updateStatus() throws Exception
    {
        for(TransferRequest r:db.requests())System.out.println(r);
        transfers.setStatus(number("Request ID: "),read("New status: ").toUpperCase());
        System.out.println("Status updated");
    }
    private static void concurrencyDemo() throws Exception
    {
        SlotManager slots=new SlotManager(2);
        Thread[] threads=new Thread[5];
        for(int i=0;i<5;i++)
        {
            threads[i]=new Thread(new TransferTask(slots),"Team-"+(i+1));
            threads[i].start();
        }
        Thread.sleep(10);
        threads[4].interrupt();
        for(Thread t:threads)t.join();
    }
    private static void raceResults() throws Exception
    {
        int id=number("Driver ID: ");
        Driver d=db.findDriver(id);
        if(d==null)throw new InvalidDriverException("Driver not found");
        System.out.println("1st=25 2nd=18 3rd=15 4th=12 5th=10 6th=8 7th=6 8th=4 9th=2 10th=1");
        int place=number("Finish position (1-10): ");
        int[] points=
        {
            25,18,15,12,10,8,6,4,2,1
        }
        ;
        if(place<1||place>10)throw new IllegalArgumentException("Position must be 1 to 10");
        db.addRaceResult(id,points[place-1]);
    }
    public static void main(String[] args)
    {
        boolean running=true;
        while(running)
        {
            try
            {
                System.out.println("\nF1 Team Management & Transfer Market System\n1 Driver Management\n2 Team Management\n3 Transfer Offer Management\n4 Check Driver Eligibility\n5 Submit Transfer Request\n6 View Requests\n7 Update Request Status\n8 Reports and Standings\n9 Import Drivers from CSV\n10 Export Report to CSV\n11 Process Pending Requests\n12 Concurrency Demo\n13 Race Results\n0 Exit");
                int choice=number("Choice: ");
                if(choice==1)driverMenu();
                else if(choice==2)teamMenu();
                else if(choice==3)offerMenu();
                else if(choice==4)transfers.checkEligibility(number("Driver ID: "),number("Offer ID: "));
                else if(choice==5)
                {
                    transfers.submit(number("Driver ID: "),number("Offer ID: "));
                    System.out.println("Request submitted");
                }
                else if(choice==6)
                {
                    for(TransferRequest r:db.requests())System.out.println(r);
                }
                else if(choice==7)updateStatus();
                else if(choice==8)System.out.print(reports.buildReport());
                else if(choice==9)CsvHelper.importDrivers(read("CSV path: "),drivers);
                else if(choice==10)
                {
                    CsvHelper.exportReport(read("Output CSV path: "),reports.buildReport());
                    System.out.println("Report exported");
                }
                else if(choice==11)
                {
                    List<TransferRequest> pending=new ArrayList<>();
                    for(TransferRequest r:db.requests())if(r.getStatus().equals("PENDING"))pending.add(r);
                    TransferTask.processPending(pending);
                }
                else if(choice==12)concurrencyDemo();
                else if(choice==13)raceResults();
                else if(choice==0)running=false;
                else System.out.println("Invalid menu choice");
            }
            catch(NumberFormatException e)
            {
                System.out.println("Enter a valid number");
            }
            catch(InvalidDriverException|DuplicateRecordException|TransferClosedException e)
            {
                System.out.println(e.getMessage());
            }
            catch(java.sql.SQLException e)
            {
                System.out.println("Database connection failed. Check MySQL and Database.java settings: "+e.getMessage());
            }
            catch(Exception e)
            {
                System.out.println("Operation failed: "+e.getMessage());
            }
        }
        input.close();
        System.out.println("Goodbye");
    }
}
