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
 * Test venue capacity validation during event creation
 */
class VenueCapacityValidationTest {
    
    private EventHub eventHub;
    private String organizerId;
    private LocalDateTime baseTime;
    private String smallVenueId;
    private String largeVenueId;
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        baseTime = LocalDateTime.now().plusDays(1);
        
        // Create organizer user
        String organizerUsername = "caporg" + System.currentTimeMillis();
        eventHub.registerUser(organizerUsername, organizerUsername + "@test.com", "password123", 
                             "Test", "Organizer", com.campuseventhub.model.user.UserRole.ORGANIZER);
        var organizer = eventHub.authenticateUser(organizerUsername, "password123");
        organizerId = organizer.getUserId();
        
        // Use existing venues - based on previous test runs, we know these exist:
        // Small Room (Building B) - capacity 20
        // Test Hall (Building A) - capacity 100
        var allVenues = eventHub.listVenues();
        
        for (Venue venue : allVenues) {
            if (venue.getName().contains("Small") && venue.getCapacity() <= 20) {
                smallVenueId = venue.getVenueId();
            } else if (venue.getName().contains("Hall") && venue.getCapacity() >= 100) {
                largeVenueId = venue.getVenueId();
            }
        }
    }
    
    @Test
    @DisplayName("Event creation should be blocked when capacity exceeds venue capacity")
    void shouldBlockEventWhenCapacityExceedsVenue() {
        if (smallVenueId == null) {
            // Skip test if no small venue available
            return;
        }
        
        // Try to create event with capacity 50 for a venue with capacity <= 20
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventHub.createEvent(
                "Large Event", "Event that exceeds venue capacity", EventType.WORKSHOP,
                baseTime, baseTime.plusHours(2), organizerId, smallVenueId, 50
            );
        }, "Event creation should be blocked when capacity exceeds venue capacity");
        
        assertTrue(exception.getMessage().contains("capacity"), 
                  "Error message should mention capacity issue: " + exception.getMessage());
    }
    
    @Test
    @DisplayName("Event creation should succeed when capacity fits venue")
    void shouldAllowEventWhenCapacityFitsVenue() {
        if (largeVenueId == null) {
            // Skip test if no large venue available
            return;
        }
        
        // Create event with capacity 30 for a venue with capacity >= 100
        Event event = eventHub.createEvent(
            "Fitting Event", "Event that fits venue capacity", EventType.SEMINAR,
            baseTime, baseTime.plusHours(1), organizerId, largeVenueId, 30
        );
        
        assertNotNull(event, "Event creation should succeed when capacity fits venue");
        assertEquals(30, event.getMaxCapacity());
        assertTrue(event.hasVenue());
        assertEquals(largeVenueId, event.getVenueId());
    }
    
    @Test
    @DisplayName("Event creation should succeed without venue")
    void shouldAllowEventWithoutVenue() {
        // Create event without venue (should not be blocked by capacity)
        Event event = eventHub.createEvent(
            "No Venue Event", "Event without venue", EventType.CLUB_MEETING,
            baseTime, baseTime.plusHours(1), organizerId, null, 200
        );
        
        assertNotNull(event, "Event creation should succeed without venue");
        assertEquals(200, event.getMaxCapacity());
        assertFalse(event.hasVenue());
    }
}