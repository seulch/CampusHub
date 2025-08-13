package com.campuseventhub.model.venue;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Represents a venue booking with full time period information
 */
public class VenueBooking implements Serializable {
    private String eventId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime bookingStart; // includes setup time
    private LocalDateTime bookingEnd; // includes cleanup time
    
    public VenueBooking(String eventId, LocalDateTime startTime, LocalDateTime endTime, 
                       int setupMinutes, int cleanupMinutes) {
        this.eventId = eventId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingStart = startTime.minusMinutes(setupMinutes);
        this.bookingEnd = endTime.plusMinutes(cleanupMinutes);
    }
    
    /**
     * Checks if this booking conflicts with the given time period
     */
    public boolean conflictsWith(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return bookingStart.isBefore(otherEnd) && bookingEnd.isAfter(otherStart);
    }
    
    /**
     * Checks if this booking conflicts with another VenueBooking
     */
    public boolean conflictsWith(VenueBooking other) {
        return conflictsWith(other.bookingStart, other.bookingEnd);
    }
    
    // Getters
    public String getEventId() { return eventId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getBookingStart() { return bookingStart; }
    public LocalDateTime getBookingEnd() { return bookingEnd; }
}