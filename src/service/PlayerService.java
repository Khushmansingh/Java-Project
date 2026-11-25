package service;

import exception.DuplicateRecordException;
import exception.InvalidInputException;
import model.Player;
import model.PlayerRole;
import model.Team;
import dao.PlayerDAO;
import dao.TeamDAO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.sql.SQLException;

public class PlayerService {
    private final List<Player> players = new ArrayList<>();
    private final Map<String, Player> playersById = new HashMap<>();
    private final Map<String, Set<Integer>> jerseyNumbersByTeam = new HashMap<>();

    public void savePlayerToDatabase(Player player) throws SQLException {
        if (new TeamDAO().findById(player.getTeamId()).isEmpty()) throw new InvalidInputException("Team ID was not found: " + player.getTeamId());
        PlayerDAO playerDAO = new PlayerDAO();
        if (playerDAO.countByTeam(player.getTeamId()) >= 25 && playerDAO.findById(player.getPlayerId()).isEmpty()) throw new InvalidInputException("A team cannot have more than 25 players.");
        if (playerDAO.jerseyExists(player.getTeamId(), player.getJerseyNumber(), player.getPlayerId())) throw new DuplicateRecordException("Jersey number already exists in this team.");
        playerDAO.save(player);
    }
    public List<Player> listPlayersFromDatabase() throws SQLException { return new PlayerDAO().findAll(); }
    public List<Player> searchDatabase(String term) throws SQLException { return new PlayerDAO().findByNameOrTeam(term); }
    public void deletePlayerFromDatabase(String id) throws SQLException { if (!new PlayerDAO().deleteById(id)) throw new InvalidInputException("Player ID was not found: " + id); }

    public void addPlayer(Player player, Team team) {
        if (player == null || team == null) throw new InvalidInputException("Player and team are required.");
        if (!player.getTeamId().equals(team.getTeamId())) throw new InvalidInputException("Player must be assigned to the selected team.");
        if (playersById.containsKey(player.getPlayerId())) throw new DuplicateRecordException("Player ID already exists.");
        Set<Integer> jerseyNumbers = jerseyNumbersByTeam.computeIfAbsent(team.getTeamId(), key -> new HashSet<>());
        if (jerseyNumbers.contains(player.getJerseyNumber())) throw new DuplicateRecordException("Jersey number already exists in this team.");
        long teamCount = players.stream().filter(existing -> existing.getTeamId().equals(team.getTeamId())).count();
        if (teamCount >= 25) throw new InvalidInputException("A team cannot have more than 25 players.");
        players.add(player);
        playersById.put(player.getPlayerId(), player);
        jerseyNumbers.add(player.getJerseyNumber());
    }

    public Player findById(String playerId) {
        Player player = playersById.get(playerId);
        if (player == null) throw new InvalidInputException("Player ID was not found: " + playerId);
        return player;
    }

    public List<Player> findByName(String name) {
        String searchText = name.trim().toLowerCase();
        return players.stream().filter(player -> player.getName().toLowerCase().contains(searchText)).collect(Collectors.toList());
    }

    public List<Player> findByTeam(String teamId) {
        return players.stream().filter(player -> player.getTeamId().equals(teamId)).collect(Collectors.toList());
    }

    public List<Player> findByTeamAndRole(String teamId, PlayerRole role) {
        return players.stream().filter(player -> player.getTeamId().equals(teamId) && player.getRole() == role).collect(Collectors.toList());
    }

    public List<Player> sortByName() {
        return players.stream().sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
    }

    public List<Player> sortByStat(Map<String, Integer> statByPlayerId) {
        return players.stream().sorted(Comparator.comparingInt(
                (Player player) -> statByPlayerId.getOrDefault(player.getPlayerId(), 0)).reversed()).collect(Collectors.toList());
    }

    public List<Player> topScorers(Map<String, Integer> statByPlayerId) {
        return topPlayersByStat(statByPlayerId, 5);
    }

    public List<Player> topPlayersByStat(Map<String, Integer> statByPlayerId, int limit) {
        if (limit < 0) throw new InvalidInputException("Top-player limit cannot be negative.");
        return sortByStat(statByPlayerId).stream().limit(limit).collect(Collectors.toList());
    }

    public List<String> playerNamesByTeamAndRole(String teamId, PlayerRole role) {
        return players.stream().filter(player -> player.getTeamId().equals(teamId) && player.getRole() == role)
                .map(Player::getName).collect(Collectors.toList());
    }

    public void updatePlayer(String playerId, PlayerRole role, int jerseyNumber) {
        Player player = findById(playerId);
        Set<Integer> jerseyNumbers = jerseyNumbersByTeam.get(player.getTeamId());
        if (jerseyNumber != player.getJerseyNumber() && jerseyNumbers.contains(jerseyNumber)) {
            throw new DuplicateRecordException("Jersey number already exists in this team.");
        }
        if (role == null || jerseyNumber < 1 || jerseyNumber > 99) throw new InvalidInputException("Role is required and jersey number must be from 1 to 99.");
        jerseyNumbers.remove(player.getJerseyNumber());
        player.updatePlayerDetails(role, jerseyNumber);
        jerseyNumbers.add(jerseyNumber);
    }

    public void transferPlayer(String playerId, Team newTeam) {
        Player player = findById(playerId);
        if (newTeam == null) throw new InvalidInputException("Destination team is required.");
        Set<Integer> newTeamJerseys = jerseyNumbersByTeam.computeIfAbsent(newTeam.getTeamId(), key -> new HashSet<>());
        if (newTeamJerseys.contains(player.getJerseyNumber())) throw new DuplicateRecordException("Jersey number already exists in the destination team.");
        if (findByTeam(newTeam.getTeamId()).size() >= 25) throw new InvalidInputException("A team cannot have more than 25 players.");
        jerseyNumbersByTeam.get(player.getTeamId()).remove(player.getJerseyNumber());
        player.transferTo(newTeam.getTeamId());
        newTeamJerseys.add(player.getJerseyNumber());
    }

    public boolean removePlayer(String playerId) {
        Player player = findById(playerId);
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getPlayerId().equals(playerId)) {
                iterator.remove();
                playersById.remove(playerId);
                jerseyNumbersByTeam.get(player.getTeamId()).remove(player.getJerseyNumber());
                return true;
            }
        }
        return false;
    }

    public List<Player> findAll() { return new ArrayList<>(players); }
}
