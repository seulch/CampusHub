package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

/**
 * Debug venue capacity validation
 */
class VenueCapacityDebugTest {
    
    private EventHub eventHub;
    private String organizerId;
    private LocalDateTime baseTime;
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        baseTime = LocalDateTime.now().plusDays(1);
        
        // Use existing organizer user
        var organizer = eventHub.authenticateUser("organizer", "organizer123");
        organizerId = organizer.getUserId();
    }
    
    @Test
    @DisplayName("Debug venue capacity validation")
    void debugVenueCapacityValidation() {
        // List all available venues
        var venues = eventHub.listVenues();
        System.out.println("Available venues:");
        for (Venue venue : venues) {
            System.out.println("- " + venue.getName() + " (ID: " + venue.getVenueId() + ", Capacity: " + venue.getCapacity() + ")");
        }
        
        // Find a small venue
        Venue smallVenue = null;
        for (Venue venue : venues) {
            if (venue.getCapacity() <= 25) {
                smallVenue = venue;
                break;
            }
        }
        
        if (smallVenue == null) {
            System.out.println("No small venue found, skipping test");
            return;
        }
        
        System.out.println("Using venue: " + smallVenue.getName() + " with capacity " + smallVenue.getCapacity());
        
        // Try to create event with capacity 100 for this small venue
        try {
            Event event = eventHub.createEvent(
                "Debug Event", "Testing capacity validation", EventType.WORKSHOP,
                baseTime, baseTime.plusHours(2), organizerId, smallVenue.getVenueId(), 100
            );
            
            if (event != null) {
                System.out.println("EVENT CREATED SUCCESSFULLY - THIS IS A BUG!");
                System.out.println("Event capacity: " + event.getMaxCapacity());
                System.out.println("Event has venue: " + event.hasVenue());
                if (event.hasVenue()) {
                    System.out.println("Event venue: " + event.getVenueName() + " (capacity: " + event.getVenueCapacity() + ")");
                }
                fail("Event creation should have been blocked due to capacity mismatch");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Event creation blocked correctly: " + e.getMessage());
            assertTrue(e.getMessage().contains("capacity"), "Error should mention capacity");
        }
    }
}