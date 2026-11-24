import java.sql.*;
import java.util.*;
public class Database
{
    public static final String URL = "jdbc:mysql://localhost:3306/f1_management";
    public static final String USER = "root";
    public static final String PASSWORD = "password";
    public Connection connect() throws SQLException
    {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    private Driver driver(ResultSet r) throws SQLException
    {
        int t=r.getInt("team_id");
        Integer team=r.wasNull()?null:t;
        return new Driver(r.getInt("driver_id"),r.getString("name"),r.getString("nationality"),r.getString("email"),r.getDouble("rating"),r.getDouble("salary"),r.getDouble("market_value"),team,r.getString("status"));
    }
    public List<Driver> drivers() throws SQLException
    {
        List<Driver> a=new ArrayList<>();
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("SELECT * FROM drivers"); ResultSet r=p.executeQuery())
        {
            while(r.next()) a.add(driver(r));
        }
        return a;
    }
    public Driver findDriver(int id) throws SQLException
    {
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("SELECT * FROM drivers WHERE driver_id=?"))
        {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery())
            {
                return r.next()?driver(r):null;
            }
        }
    }
    public void saveDriver(Driver d) throws SQLException
    {
        String q="INSERT INTO drivers(driver_id,name,nationality,email,rating,salary,market_value,team_id,status) VALUES(?,?,?,?,?,?,?,?,?)";
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement(q))
        {
            p.setInt(1,d.getDriverId());
            p.setString(2,d.getName());
            p.setString(3,d.getNationality());
            p.setString(4,d.getEmail());
            p.setDouble(5,d.getRating());
            p.setDouble(6,d.getSalary());
            p.setDouble(7,d.getMarketValue());
            if(d.getTeamId()==null)p.setNull(8,Types.INTEGER);
            else p.setInt(8,d.getTeamId());
            p.setString(9,d.getStatus());
            p.executeUpdate();
        }
    }
    public void updateDriver(Driver d) throws SQLException
    {
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("UPDATE drivers SET name=?,nationality=?,email=?,rating=?,salary=?,market_value=?,status=? WHERE driver_id=?"))
        {
            p.setString(1,d.getName());
            p.setString(2,d.getNationality());
            p.setString(3,d.getEmail());
            p.setDouble(4,d.getRating());
            p.setDouble(5,d.getSalary());
            p.setDouble(6,d.getMarketValue());
            p.setString(7,d.getStatus());
            p.setInt(8,d.getDriverId());
            p.executeUpdate();
        }
    }
    public List<Team> teams() throws SQLException
    {
        List<Team>a=new ArrayList<>();
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM teams");ResultSet r=p.executeQuery())
        {
            while(r.next())a.add(new Team(r.getInt("team_id"),r.getString("team_name"),r.getString("country"),r.getDouble("budget"),r.getInt("max_seats")));
        }
        return a;
    }
    public Team findTeam(int id) throws SQLException
    {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM teams WHERE team_id=?"))
        {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery())
            {
                return r.next()?new Team(r.getInt("team_id"),r.getString("team_name"),r.getString("country"),r.getDouble("budget"),r.getInt("max_seats")):null;
            }
        }
    }
    public void saveTeam(Team t) throws SQLException
    {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO teams VALUES(?,?,?,?,?)"))
        {
            p.setInt(1,t.getTeamId());
            p.setString(2,t.getTeamName());
            p.setString(3,t.getCountry());
            p.setDouble(4,t.getBudget());
            p.setInt(5,t.getMaxSeats());
            p.executeUpdate();
        }
    }
    public void saveOffer(TransferOffer o) throws SQLException
    {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO transfer_offers(team_id,offered_salary,offer_amount,deadline,min_rating,is_open) VALUES(?,?,?,?,?,?)"))
        {
            p.setInt(1,o.getTeamId());
            p.setDouble(2,o.getOfferedSalary());
            p.setDouble(3,o.getOfferAmount());
            p.setDate(4,java.sql.Date.valueOf(o.getDeadline()));
            p.setDouble(5,o.getMinRating());
            p.setBoolean(6,o.isOpen());
            p.executeUpdate();
        }
    }
    public TransferOffer findOffer(int id) throws SQLException
    {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM transfer_offers WHERE offer_id=?"))
        {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery())
            {
                return r.next()?new TransferOffer(r.getInt("offer_id"),r.getInt("team_id"),r.getDouble("offered_salary"),r.getDouble("offer_amount"),r.getDate("deadline").toLocalDate(),r.getDouble("min_rating"),r.getBoolean("is_open")):null;
            }
        }
    }
    public List<TransferOffer> offers() throws SQLException
    {
        List<TransferOffer>a=new ArrayList<>();
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT offer_id FROM transfer_offers");ResultSet r=p.executeQuery())
        {
            while(r.next())a.add(findOffer(r.getInt(1)));
        }
        return a;
    }
    public void saveRequest(int driver,int offer) throws SQLException
    {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO transfer_requests(driver_id,offer_id,status) VALUES(?,?,'PENDING')"))
        {
            p.setInt(1,driver);
            p.setInt(2,offer);
            p.executeUpdate();
        }
    }
    public List<TransferRequest> requests() throws SQLException
    {
        List<TransferRequest>a=new ArrayList<>();
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM transfer_requests");ResultSet r=p.executeQuery())
        {
            while(r.next())a.add(new TransferRequest(r.getInt("request_id"),r.getInt("driver_id"),r.getInt("offer_id"),r.getString("status")));
        }
        return a;
    }
    public void updateRequestStatus(int request,int driver,int offer,String status) throws Exception
    {
        try(Connection c=connect())
        {
            c.setAutoCommit(false);
            try
            {
                if(status.equals("SIGNED"))
                {
                    String lock="SELECT t.team_id,t.budget,t.max_seats,o.offered_salary,o.offer_amount FROM teams t JOIN transfer_offers o ON o.team_id=t.team_id WHERE o.offer_id=? FOR UPDATE";
                    int teamId;
                    double budget,salary,amount;
                    int seats;
                    try(PreparedStatement p=c.prepareStatement(lock))
                    {
                        p.setInt(1,offer);
                        try(ResultSet r=p.executeQuery())
                        {
                            if(!r.next())throw new Exception("Offer or team not found");
                            teamId=r.getInt(1);
                            budget=r.getDouble(2);
                            seats=r.getInt(3);
                            salary=r.getDouble(4);
                            amount=r.getDouble(5);
                        }
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT driver_id FROM drivers WHERE team_id=? FOR UPDATE"))
                    {
                        p.setInt(1,teamId);
                        try(ResultSet r=p.executeQuery())
                        {
                            int count=0;
                            while(r.next())count++;
                            if(count>=seats)throw new Exception("Team has no free seat");
                        }
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT status,team_id FROM drivers WHERE driver_id=? FOR UPDATE"))
                    {
                        p.setInt(1,driver);
                        try(ResultSet r=p.executeQuery())
                        {
                            if(!r.next()||!r.getString(1).equals("FIT"))throw new Exception("Driver is missing or not fit");
                            if(r.getObject(2)!=null)throw new Exception("Driver has already signed with a team");
                        }
                    }
                    if(budget<amount+salary)throw new Exception("Team budget is insufficient");
                    try(PreparedStatement p=c.prepareStatement("UPDATE drivers SET team_id=?,salary=? WHERE driver_id=?"))
                    {
                        p.setInt(1,teamId);
                        p.setDouble(2,salary);
                        p.setInt(3,driver);
                        p.executeUpdate();
                    }
                    try(PreparedStatement p=c.prepareStatement("UPDATE teams SET budget=budget-? WHERE team_id=?"))
                    {
                        p.setDouble(1,amount+salary);
                        p.setInt(2,teamId);
                        p.executeUpdate();
                    }
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE transfer_requests SET status=? WHERE request_id=?"))
                {
                    p.setString(1,status);
                    p.setInt(2,request);
                    if(p.executeUpdate()!=1)throw new Exception("Request not found");
                }
                c.commit();
            }
            catch(Exception e)
            {
                c.rollback();
                throw e;
            }
            finally
            {
                c.setAutoCommit(true);
            }
        }
    }
    public void addRaceResult(int driver,int points) throws SQLException
    {
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO race_results(driver_id,points) VALUES(?,?) ON DUPLICATE KEY UPDATE points=points+VALUES(points)"))
        {
            p.setInt(1,driver);
            p.setInt(2,points);
            p.executeUpdate();
        }
    }
    public Map<Integer,Integer> standings() throws SQLException
    {
        Map<Integer,Integer>m=new LinkedHashMap<>();
        try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT driver_id,points FROM race_results ORDER BY points DESC");ResultSet r=p.executeQuery())
        {
            while(r.next())m.put(r.getInt(1),r.getInt(2));
        }
        return m;
    }
}
