package model;

import exception.InvalidInputException;

public class Player extends Person {
    private final String playerId;
    private PlayerRole role;
    private int jerseyNumber;
    private String teamId;

    public Player(String playerId, String name, int age, PlayerRole role, int jerseyNumber, String teamId) {
        super(name, age);
        this.playerId = requireText(playerId, "Player ID");
        if (role == null) throw new InvalidInputException("Player role is required.");
        if (jerseyNumber < 1 || jerseyNumber > 99) throw new InvalidInputException("Jersey number must be from 1 to 99.");
        this.role = role;
        this.jerseyNumber = jerseyNumber;
        this.teamId = requireText(teamId, "Team ID");
    }

    public void transferTo(String newTeamId) { this.teamId = requireText(newTeamId, "Team ID"); }

    public void updatePlayerDetails(PlayerRole role, int jerseyNumber) {
        if (role == null) throw new InvalidInputException("Player role is required.");
        if (jerseyNumber < 1 || jerseyNumber > 99) throw new InvalidInputException("Jersey number must be from 1 to 99.");
        this.role = role;
        this.jerseyNumber = jerseyNumber;
    }
    public String getPlayerId() { return playerId; }
    public PlayerRole getRole() { return role; }
    public int getJerseyNumber() { return jerseyNumber; }
    public String getTeamId() { return teamId; }

    @Override
    public String getDisplayInfo() {
        return super.getDisplayInfo() + " | " + role + " | #" + jerseyNumber + " | team " + teamId;
    }
}
