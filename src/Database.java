import java.sql.*;
import java.util.*;

public class Database {

    public static final String URL = "jdbc:mysql://localhost:3306/f1_management";
    public static final String USER = "root";
    public static final String PASSWORD = "root";

    public Connection connect() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
        return con;
    }

    private Driver makeDriver(ResultSet rs) throws SQLException {
        int id = rs.getInt("driver_id");
        String name = rs.getString("name");
        String nationality = rs.getString("nationality");
        String email = rs.getString("email");
        double rating = rs.getDouble("rating");
        double salary = rs.getDouble("salary");
        double marketValue = rs.getDouble("market_value");
        String status = rs.getString("status");

        int teamIdNumber = rs.getInt("team_id");
        Integer teamId;
        if (rs.wasNull()) {
            teamId = null;
        } else {
            teamId = teamIdNumber;
        }

        Driver d = new Driver(id, name, nationality, email, rating, salary, marketValue, teamId, status);
        return d;
    }

    public List<Driver> drivers() throws SQLException {
        List<Driver> list = new ArrayList<>();

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM drivers");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Driver d = makeDriver(rs);
            list.add(d);
        }

        rs.close();
        ps.close();
        con.close();
        return list;
    }

    public Driver findDriver(int id) throws SQLException {
        Driver d = null;

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM drivers WHERE driver_id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            d = makeDriver(rs);
        }

        rs.close();
        ps.close();
        con.close();
        return d;
    }

    public void saveDriver(Driver d) throws SQLException {
        String sql = "INSERT INTO drivers(driver_id, name, nationality, email, rating, salary, market_value, team_id, status) VALUES(?,?,?,?,?,?,?,?,?)";

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, d.getDriverId());
        ps.setString(2, d.getName());
        ps.setString(3, d.getNationality());
        ps.setString(4, d.getEmail());
        ps.setDouble(5, d.getRating());
        ps.setDouble(6, d.getSalary());
        ps.setDouble(7, d.getMarketValue());

        if (d.getTeamId() == null) {
            ps.setNull(8, Types.INTEGER);
        } else {
            ps.setInt(8, d.getTeamId());
        }

        ps.setString(9, d.getStatus());
        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public void updateDriver(Driver d) throws SQLException {
        String sql = "UPDATE drivers SET name = ?, nationality = ?, email = ?, rating = ?, salary = ?, market_value = ?, status = ? WHERE driver_id = ?";

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, d.getName());
        ps.setString(2, d.getNationality());
        ps.setString(3, d.getEmail());
        ps.setDouble(4, d.getRating());
        ps.setDouble(5, d.getSalary());
        ps.setDouble(6, d.getMarketValue());
        ps.setString(7, d.getStatus());
        ps.setInt(8, d.getDriverId());
        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public List<Team> teams() throws SQLException {
        List<Team> list = new ArrayList<>();

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM teams");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("team_id");
            String name = rs.getString("team_name");
            String country = rs.getString("country");
            double budget = rs.getDouble("budget");
            int maxSeats = rs.getInt("max_seats");

            Team t = new Team(id, name, country, budget, maxSeats);
            list.add(t);
        }

        rs.close();
        ps.close();
        con.close();
        return list;
    }

    public Team findTeam(int id) throws SQLException {
        Team t = null;

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM teams WHERE team_id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            t = new Team(rs.getInt("team_id"), rs.getString("team_name"), rs.getString("country"),
                    rs.getDouble("budget"), rs.getInt("max_seats"));
        }

        rs.close();
        ps.close();
        con.close();
        return t;
    }

    public void saveTeam(Team t) throws SQLException {
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("INSERT INTO teams VALUES(?,?,?,?,?)");
        ps.setInt(1, t.getTeamId());
        ps.setString(2, t.getTeamName());
        ps.setString(3, t.getCountry());
        ps.setDouble(4, t.getBudget());
        ps.setInt(5, t.getMaxSeats());
        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public void saveOffer(TransferOffer o) throws SQLException {
        String sql = "INSERT INTO transfer_offers(team_id, offered_salary, offer_amount, deadline, min_rating, is_open) VALUES(?,?,?,?,?,?)";

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, o.getTeamId());
        ps.setDouble(2, o.getOfferedSalary());
        ps.setDouble(3, o.getOfferAmount());
        ps.setDate(4, java.sql.Date.valueOf(o.getDeadline()));
        ps.setDouble(5, o.getMinRating());
        ps.setBoolean(6, o.isOpen());
        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public TransferOffer findOffer(int id) throws SQLException {
        TransferOffer offer = null;

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM transfer_offers WHERE offer_id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int offerId = rs.getInt("offer_id");
            int teamId = rs.getInt("team_id");
            double salary = rs.getDouble("offered_salary");
            double amount = rs.getDouble("offer_amount");
            java.time.LocalDate deadline = rs.getDate("deadline").toLocalDate();
            double minRating = rs.getDouble("min_rating");
            boolean isOpen = rs.getBoolean("is_open");

            offer = new TransferOffer(offerId, teamId, salary, amount, deadline, minRating, isOpen);
        }

        rs.close();
        ps.close();
        con.close();
        return offer;
    }

    public List<TransferOffer> offers() throws SQLException {
        List<TransferOffer> list = new ArrayList<>();

        List<Integer> ids = new ArrayList<>();
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT offer_id FROM transfer_offers");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            ids.add(rs.getInt(1));
        }

        rs.close();
        ps.close();
        con.close();

        for (int i = 0; i < ids.size(); i++) {
            TransferOffer o = findOffer(ids.get(i));
            list.add(o);
        }

        return list;
    }

    public void saveRequest(int driver, int offer) throws SQLException {
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO transfer_requests(driver_id, offer_id, status) VALUES(?, ?, 'PENDING')");
        ps.setInt(1, driver);
        ps.setInt(2, offer);
        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public List<TransferRequest> requests() throws SQLException {
        List<TransferRequest> list = new ArrayList<>();

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM transfer_requests");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            TransferRequest r = new TransferRequest(rs.getInt("request_id"), rs.getInt("driver_id"),
                    rs.getInt("offer_id"), rs.getString("status"));
            list.add(r);
        }

        rs.close();
        ps.close();
        con.close();
        return list;
    }

    public void updateRequestStatus(int request, int driver, int offer, String status) throws Exception {
        Connection con = connect();

        con.setAutoCommit(false);

        try {
            if (status.equals("SIGNED")) {

                String sql1 = "SELECT t.team_id, t.budget, t.max_seats, o.offered_salary, o.offer_amount "
                        + "FROM teams t JOIN transfer_offers o ON o.team_id = t.team_id WHERE o.offer_id = ?";
                PreparedStatement ps1 = con.prepareStatement(sql1);
                ps1.setInt(1, offer);
                ResultSet rs1 = ps1.executeQuery();

                if (!rs1.next()) {
                    throw new Exception("Offer or team not found");
                }

                int teamId = rs1.getInt(1);
                double budget = rs1.getDouble(2);
                int seats = rs1.getInt(3);
                double salary = rs1.getDouble(4);
                double amount = rs1.getDouble(5);
                rs1.close();
                ps1.close();

                PreparedStatement ps2 = con.prepareStatement("SELECT COUNT(*) FROM drivers WHERE team_id = ?");
                ps2.setInt(1, teamId);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int count = rs2.getInt(1);
                rs2.close();
                ps2.close();

                if (count >= seats) {
                    throw new Exception("Team has no free seat");
                }

                PreparedStatement ps3 = con.prepareStatement("SELECT status, team_id FROM drivers WHERE driver_id = ?");
                ps3.setInt(1, driver);
                ResultSet rs3 = ps3.executeQuery();

                if (!rs3.next()) {
                    throw new Exception("Driver is missing or not fit");
                }
                String driverStatus = rs3.getString(1);
                rs3.getInt(2);
                boolean hasTeam = !rs3.wasNull();
                rs3.close();
                ps3.close();

                if (!driverStatus.equals("FIT")) {
                    throw new Exception("Driver is missing or not fit");
                }
                if (hasTeam) {
                    throw new Exception("Driver has already signed with a team");
                }

                double totalCost = amount + salary;
                if (budget < totalCost) {
                    throw new Exception("Team budget is insufficient");
                }

                PreparedStatement ps4 = con.prepareStatement("UPDATE drivers SET team_id = ?, salary = ? WHERE driver_id = ?");
                ps4.setInt(1, teamId);
                ps4.setDouble(2, salary);
                ps4.setInt(3, driver);
                ps4.executeUpdate();
                ps4.close();

                PreparedStatement ps5 = con.prepareStatement("UPDATE teams SET budget = budget - ? WHERE team_id = ?");
                ps5.setDouble(1, totalCost);
                ps5.setInt(2, teamId);
                ps5.executeUpdate();
                ps5.close();
            }

            PreparedStatement ps6 = con.prepareStatement("UPDATE transfer_requests SET status = ? WHERE request_id = ?");
            ps6.setString(1, status);
            ps6.setInt(2, request);
            int rows = ps6.executeUpdate();
            ps6.close();

            if (rows != 1) {
                throw new Exception("Request not found");
            }

            con.commit();

        } catch (Exception e) {
            con.rollback();
            con.setAutoCommit(true);
            con.close();
            throw e;
        }

        con.setAutoCommit(true);
        con.close();
    }

    public void addRaceResult(int driver, int points) throws SQLException {
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(
                "INSERT INTO race_results(driver_id, points) VALUES(?, ?) ON DUPLICATE KEY UPDATE points = points + VALUES(points)");
        ps.setInt(1, driver);
        ps.setInt(2, points);
        ps.executeUpdate();

        ps.close();
        con.close();
    }

    public Map<Integer, Integer> standings() throws SQLException {
        Map<Integer, Integer> map = new LinkedHashMap<>();

        Connection con = connect();
        PreparedStatement ps = con.prepareStatement("SELECT driver_id, points FROM race_results ORDER BY points DESC");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            map.put(rs.getInt(1), rs.getInt(2));
        }

        rs.close();
        ps.close();
        con.close();
        return map;
    }
}