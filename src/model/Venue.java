package model;

import exception.InvalidInputException;

public class Venue {
    private final String venueId;
    private final String name;
    private final String city;
    private final int capacity;

    public Venue(String venueId, String name, String city, int capacity) {
        this.venueId = Person.requireText(venueId, "Venue ID");
        this.name = Person.requireText(name, "Venue name");
        this.city = Person.requireText(city, "City");
        if (capacity < 1) throw new InvalidInputException("Venue capacity must be positive.");
        this.capacity = capacity;
    }

    public String getVenueId() { return venueId; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getCapacity() { return capacity; }
}
