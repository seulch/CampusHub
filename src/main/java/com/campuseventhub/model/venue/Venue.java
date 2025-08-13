// =============================================================================
// VENUE MODEL
// =============================================================================

package com.campuseventhub.model.venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;

/**
 * Venue model for managing physical event locations.
 * 
 * Implementation Details:
 * - Comprehensive venue information storage
 * - Availability tracking and conflict prevention
 * - Equipment and capacity management
 * - Booking system integration
 * - Multi-timezone support for scheduling
 * - Maintenance and setup time considerations
 */
public class Venue implements Serializable {
    private String venueId;
    private String name;
    private String location;
    private String building;
    private String floor;
    private String roomNumber;
    private int capacity;
    private List<String> equipment; // Projector, Microphone, etc.
    private Map<String, VenueBooking> bookings; // EventId -> VenueBooking
    private List<String> features; // WiFi, AC, Accessible, etc.
    private int setupTimeMinutes;
    private int cleanupTimeMinutes;
    private boolean isActive;
    
    public Venue(String name, String location, int capacity) {
        this.venueId = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.equipment = new ArrayList<>();
        this.bookings = new HashMap<>();
        this.features = new ArrayList<>();
        this.setupTimeMinutes = 30;
        this.cleanupTimeMinutes = 30;
        this.isActive = true;
    }
    
    public boolean isAvailable(LocalDateTime startTime, LocalDateTime endTime) {
        if (!isActive) {
            return false;
        }
        
        // Include setup and cleanup time in the check
        LocalDateTime requestedStart = startTime.minusMinutes(setupTimeMinutes);
        LocalDateTime requestedEnd = endTime.plusMinutes(cleanupTimeMinutes);
        
        // Check for overlapping bookings
        for (VenueBooking booking : bookings.values()) {
            if (booking.conflictsWith(requestedStart, requestedEnd)) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean bookVenue(String eventId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!isAvailable(startTime, endTime)) {
            return false;
        }
        
        VenueBooking booking = new VenueBooking(eventId, startTime, endTime, setupTimeMinutes, cleanupTimeMinutes);
        bookings.put(eventId, booking);
        return true;
    }
    
    public boolean cancelBooking(String eventId) {
        return bookings.remove(eventId) != null;
    }
    
    // Getters and setters
    public String getVenueId() { return venueId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getBuilding() { return building; }
    public String getFloor() { return floor; }
    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public List<String> getEquipment() { return equipment; }
    public Map<String, VenueBooking> getBookings() { return bookings; }
    public List<String> getFeatures() { return features; }
    public int getSetupTimeMinutes() { return setupTimeMinutes; }
    public int getCleanupTimeMinutes() { return cleanupTimeMinutes; }
    public boolean isActive() { return isActive; }
    
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setBuilding(String building) { this.building = building; }
    public void setFloor(String floor) { this.floor = floor; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment; }
    public void setFeatures(List<String> features) { this.features = features; }
    public void setSetupTimeMinutes(int setupTimeMinutes) { this.setupTimeMinutes = setupTimeMinutes; }
    public void setCleanupTimeMinutes(int cleanupTimeMinutes) { this.cleanupTimeMinutes = cleanupTimeMinutes; }
    public void setActive(boolean active) { isActive = active; }
}