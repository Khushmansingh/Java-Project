package service;

import dao.VenueDAO;
import model.Venue;
import java.sql.SQLException;
import java.util.List;

public class VenueService {
    private final VenueDAO venueDAO;
    public VenueService(VenueDAO venueDAO) { this.venueDAO = venueDAO; }
    public void addVenue(Venue venue) throws SQLException { venueDAO.save(venue); }
    public List<Venue> listVenues() throws SQLException { return venueDAO.findAll(); }
}
