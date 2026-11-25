package service;

import exception.DuplicateRecordException;
import exception.InvalidInputException;
import model.Player;
import model.Team;
import dao.TeamDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.sql.SQLException;
import java.util.Locale;

public class TeamService {
    private final List<Team> teams = new ArrayList<>();
    private final Map<String, Team> teamsById = new HashMap<>();
    private final Set<String> normalizedNames = new HashSet<>();

    public void saveTeamToDatabase(Team team) throws SQLException { new TeamDAO().save(team); }
    public void assignCaptainToDatabase(String teamId, String playerId) throws SQLException { new TeamDAO().assignCaptain(teamId, playerId); }
    public List<Team> listTeamsFromDatabase() throws SQLException { return new TeamDAO().findAll(); }
    public Team findTeamInDatabase(String teamId) throws SQLException { return new TeamDAO().findById(teamId).orElseThrow(() -> new InvalidInputException("Team ID was not found: " + teamId)); }
    public void deleteTeamFromDatabase(String teamId) throws SQLException {
        try {
            if (!new TeamDAO().deleteById(teamId)) throw new InvalidInputException("Team ID was not found: " + teamId);
        } catch (SQLException exception) {
            if (exception.getSQLState() != null && exception.getSQLState().startsWith("23")) throw new InvalidInputException("Team has players or matches and cannot be deleted.");
            throw exception;
        }
    }

    public void addTeam(Team team) {
        if (team == null) throw new InvalidInputException("Team is required.");
        if (teamsById.containsKey(team.getTeamId())) throw new DuplicateRecordException("Team ID already exists.");
        String key = team.getName().toLowerCase(Locale.ROOT);
        if (!normalizedNames.add(key)) throw new DuplicateRecordException("A team with that name already exists.");
        teams.add(team);
        teamsById.put(team.getTeamId(), team);
    }

    public Team findById(String teamId) {
        Team team = teamsById.get(teamId);
        if (team == null) throw new InvalidInputException("Team ID was not found: " + teamId);
        return team;
    }

    public List<Team> findAll() { return new ArrayList<>(teams); }

    public void updateTeam(String teamId, String name, String city, String coach) {
        Team team = findById(teamId);
        if (name == null || name.trim().isEmpty()) throw new InvalidInputException("Team name cannot be blank.");
        String newKey = name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        String oldKey = team.getName().toLowerCase(Locale.ROOT);
        if (!oldKey.equals(newKey) && normalizedNames.contains(newKey)) {
            throw new DuplicateRecordException("A team with that name already exists.");
        }
        team.updateDetails(name, city, coach);
        normalizedNames.remove(oldKey);
        normalizedNames.add(team.getName().toLowerCase(Locale.ROOT));
    }

    public boolean removeTeam(String teamId, List<Player> players, Set<String> teamsWithMatches) {
        Team team = findById(teamId);
        if (teamsWithMatches.contains(teamId)) throw new InvalidInputException("Team has matches and cannot be deleted.");
        for (Player player : players) {
            if (player.getTeamId().equals(teamId)) throw new InvalidInputException("Team has players and cannot be deleted.");
        }
        Iterator<Team> iterator = teams.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getTeamId().equals(teamId)) {
                iterator.remove();
                teamsById.remove(teamId);
                normalizedNames.remove(team.getName().toLowerCase(Locale.ROOT));
                return true;
            }
        }
        return false;
    }
}
