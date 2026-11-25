package service;

import concurrency.VenueSlotManager;
import dao.MatchDAO;
import exception.SchedulingConflictException;
import model.Match;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MatchService {
    private final MatchDAO matchDAO;
    private final VenueSlotManager venueSlotManager;
    public MatchService(MatchDAO matchDAO, VenueSlotManager venueSlotManager) { this.matchDAO = matchDAO; this.venueSlotManager = venueSlotManager; }

    public void schedule(Match match) throws SQLException {
        List<Match> currentMatches = matchDAO.findAll();
        for (Match existing : currentMatches) {
            boolean sameDay = existing.getDate().equals(match.getDate());
            boolean sharesTeam = existing.getHomeTeamId().equals(match.getHomeTeamId()) || existing.getAwayTeamId().equals(match.getHomeTeamId()) || existing.getHomeTeamId().equals(match.getAwayTeamId()) || existing.getAwayTeamId().equals(match.getAwayTeamId());
            if (sameDay && sharesTeam) throw new SchedulingConflictException("A team already has a match on this date.");
        }
        if (!venueSlotManager.tryBook(match.getVenueId(), match.getDate(), match.getTime())) throw new SchedulingConflictException("Venue time slot is already booked.");
        try { matchDAO.save(match); }
        catch (SQLException exception) { venueSlotManager.cancelBooking(match.getVenueId(), match.getDate(), match.getTime()); throw exception; }
    }

    public List<Match> listMatches() throws SQLException { return matchDAO.findAll(); }
}
