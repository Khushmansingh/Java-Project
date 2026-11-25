package concurrency;

import exception.SchedulingConflictException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class VenueSlotManager {
    private final Set<String> bookedSlots = new HashSet<>();
    // The manager object (this) is the one lock protecting the whole check-and-book operation.
    public synchronized boolean tryBook(String venueId, LocalDate date, LocalTime time) {
        String slotKey = venueId + "|" + date + "|" + time;
        if (bookedSlots.contains(slotKey)) return false;
        bookedSlots.add(slotKey);
        return true;
    }

    public synchronized void bookOrThrow(String venueId, LocalDate date, LocalTime time) {
        if (!tryBook(venueId, date, time)) throw new SchedulingConflictException("Venue time slot is already booked.");
    }

    public synchronized void cancelBooking(String venueId, LocalDate date, LocalTime time) {
        bookedSlots.remove(venueId + "|" + date + "|" + time);
    }
}
