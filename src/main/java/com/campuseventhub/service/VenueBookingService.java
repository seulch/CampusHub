package com.campuseventhub.service;

import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.model.event.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for managing venue bookings and availability.
 * 
 * Handles venue-event integration, capacity validation, and conflict detection.
 */
public class VenueBookingService {
    private VenueManager venueManager;
    
    public VenueBookingService(VenueManager venueManager) {
        this.venueManager = venueManager;
    }
    
    /**
     * Books a venue for an event with comprehensive validation
     */
    public boolean bookVenueForEvent(Event event, String venueId) {
        if (event == null || venueId == null) {
            throw new IllegalArgumentException("Event and venue ID cannot be null");
        }
        
        Venue venue = venueManager.findById(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("Venue not found: " + venueId);
        }
        
        // Validate venue capacity against event capacity
        if (event.getMaxCapacity() > venue.getCapacity()) {
            throw new IllegalArgumentException(
                String.format("Event capacity (%d) exceeds venue capacity (%d)", 
                    event.getMaxCapacity(), venue.getCapacity()));
        }
        
        // Check venue availability including setup/cleanup time
        LocalDateTime bookingStart = event.getStartDateTime().minusMinutes(venue.getSetupTimeMinutes());
        LocalDateTime bookingEnd = event.getEndDateTime().plusMinutes(venue.getCleanupTimeMinutes());
        
        if (!venue.isAvailable(bookingStart, bookingEnd)) {
            throw new IllegalArgumentException("Venue is not available for the requested time slot");
        }
        
        // Book the venue
        boolean booked = venue.bookVenue(event.getEventId(), bookingStart, bookingEnd);
        if (booked) {
            event.setVenue(venue);
            // Ensure event capacity doesn't exceed venue capacity
            if (event.getMaxCapacity() <= 0 || event.getMaxCapacity() > venue.getCapacity()) {
                event.setMaxCapacity(venue.getCapacity());
            }
        }
        
        return booked;
    }
    
    /**
     * Cancels a venue booking for an event
     */
    public boolean cancelVenueBooking(Event event) {
        if (event == null || event.getVenue() == null) {
            return false;
        }
        
        Venue venue = event.getVenue();
        boolean cancelled = venue.cancelBooking(event.getEventId());
        if (cancelled) {
            event.setVenue(null);
        }
        
        return cancelled;
    }
    
    /**
     * Finds available venues for a given time slot and capacity requirement
     */
    public List<Venue> findAvailableVenues(LocalDateTime startTime, LocalDateTime endTime, int minCapacity) {
        List<Venue> availableVenues = new ArrayList<>();
        List<Venue> allVenues = venueManager.findAll();
        
        for (Venue venue : allVenues) {
            if (!venue.isActive()) {
                continue;
            }
            
            // Check capacity requirement
            if (venue.getCapacity() < minCapacity) {
                continue;
            }
            
            // Check availability including setup/cleanup time
            LocalDateTime bookingStart = startTime.minusMinutes(venue.getSetupTimeMinutes());
            LocalDateTime bookingEnd = endTime.plusMinutes(venue.getCleanupTimeMinutes());
            
            if (venue.isAvailable(bookingStart, bookingEnd)) {
                availableVenues.add(venue);
            }
        }
        
        return availableVenues;
    }
    
    /**
     * Validates if a venue change is possible for an existing event
     */
    public boolean canChangeVenue(Event event, String newVenueId) {
        if (event == null || newVenueId == null) {
            return false;
        }
        
        Venue newVenue = venueManager.findById(newVenueId);
        if (newVenue == null || !newVenue.isActive()) {
            return false;
        }
        
        // Check capacity compatibility
        if (event.getMaxCapacity() > newVenue.getCapacity()) {
            return false;
        }
        
        // Check if current registrations fit in new venue
        int currentRegistrations = event.getRegistrations() != null ? event.getRegistrations().size() : 0;
        if (currentRegistrations > newVenue.getCapacity()) {
            return false;
        }
        
        // Check availability
        LocalDateTime bookingStart = event.getStartDateTime().minusMinutes(newVenue.getSetupTimeMinutes());
        LocalDateTime bookingEnd = event.getEndDateTime().plusMinutes(newVenue.getCleanupTimeMinutes());
        
        return newVenue.isAvailable(bookingStart, bookingEnd);
    }
    
    /**
     * Changes venue for an existing event
     */
    public boolean changeEventVenue(Event event, String newVenueId) {
        if (!canChangeVenue(event, newVenueId)) {
            return false;
        }
        
        // Cancel current booking if exists
        if (event.getVenue() != null) {
            cancelVenueBooking(event);
        }
        
        // Book new venue
        return bookVenueForEvent(event, newVenueId);
    }
    
    /**
     * Gets venue booking conflicts for a specific time period
     */
    public List<String> getVenueConflicts(String venueId, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> conflicts = new ArrayList<>();
        Venue venue = venueManager.findById(venueId);
        
        if (venue == null) {
            conflicts.add("Venue not found");
            return conflicts;
        }
        
        if (!venue.isActive()) {
            conflicts.add("Venue is not active");
        }
        
        if (!venue.isAvailable(startTime, endTime)) {
            conflicts.add("Venue is already booked for this time period");
        }
        
        return conflicts;
    }
}