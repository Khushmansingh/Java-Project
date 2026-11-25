package model;

import exception.InvalidInputException;

public class Team {
    private final String teamId;
    private String name;
    private String city;
    private String coach;
    private Player captain;

    public Team(String teamId, String name, String city, String coach) {
        this.teamId = Person.requireText(teamId, "Team ID");
        this.name = Person.requireText(name, "Team name");
        this.city = Person.requireText(city, "City");
        this.coach = Person.requireText(coach, "Coach");
    }

    public void appointCaptain(Player player) {
        if (player == null || !teamId.equals(player.getTeamId())) {
            throw new InvalidInputException("Captain must belong to this team.");
        }
        captain = player;
    }

    public void updateDetails(String name, String city, String coach) {
        this.name = Person.requireText(name, "Team name");
        this.city = Person.requireText(city, "City");
        this.coach = Person.requireText(coach, "Coach");
    }

    public String getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCoach() { return coach; }
    public Player getCaptain() { return captain; }
}
