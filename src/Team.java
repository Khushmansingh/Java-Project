public class Team
{
    private int teamId;
    private String teamName;
    private String country;
    private double budget;
    private int maxSeats;
    public Team(int teamId, String teamName, String country, double budget, int maxSeats)
    {
        this.teamId=teamId;
        this.teamName=teamName;
        this.country=country;
        this.budget=budget;
        this.maxSeats=maxSeats;
    }
    public int getTeamId()
    {
        return teamId;
    }
    public String getTeamName()
    {
        return teamName;
    }
    public String getCountry()
    {
        return country;
    }
    public double getBudget()
    {
        return budget;
    }
    public int getMaxSeats()
    {
        return maxSeats;
    }
    public String toString()
    {
        return teamId + " | " + teamName + " | " + country + " | budget=" + budget + " | seats=" + maxSeats;
    }
}
