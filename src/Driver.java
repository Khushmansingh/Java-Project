public class Driver
{
    private int driverId;
    private String name;
    private String nationality;
    private String email;
    private double rating;
    private double salary;
    private double marketValue;
    private Integer teamId;
    private String status;
    public Driver(int driverId, String name, String nationality, String email, double rating, double salary, double marketValue, Integer teamId, String status)
    {
        this.driverId = driverId;
        this.name = name;
        this.nationality = nationality;
        this.email = email;
        this.rating = rating;
        this.salary = salary;
        this.marketValue = marketValue;
        this.teamId = teamId;
        this.status = status;
    }
    public int getDriverId()
    {
        return driverId;
    }
    public String getName()
    {
        return name;
    }
    public String getNationality()
    {
        return nationality;
    }
    public String getEmail()
    {
        return email;
    }
    public double getRating()
    {
        return rating;
    }
    public double getSalary()
    {
        return salary;
    }
    public double getMarketValue()
    {
        return marketValue;
    }
    public Integer getTeamId()
    {
        return teamId;
    }
    public String getStatus()
    {
        return status;
    }
    public void setName(String value)
    {
        name = value;
    }
    public void setNationality(String value)
    {
        nationality = value;
    }
    public void setEmail(String value)
    {
        email = value;
    }
    public void setRating(double value)
    {
        rating = value;
    }
    public void setSalary(double value)
    {
        salary = value;
    }
    public void setMarketValue(double value)
    {
        marketValue = value;
    }
    public void setTeamId(Integer value)
    {
        teamId = value;
    }
    public void setStatus(String value)
    {
        status = value;
    }
    public String toString()
    {
        return driverId + " | " + name + " | " + nationality + " | " + rating + " | " + status + " | team=" + teamId;
    }
}
