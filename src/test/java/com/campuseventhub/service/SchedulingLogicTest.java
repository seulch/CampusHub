package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

/**
 * Test scheduling logic fixes:
 * - Organizers can create multiple events at same time in different venues
 * - Attendees are blocked from registering for overlapping events
 */
class SchedulingLogicTest {
    
    private EventHub eventHub;
    private String organizerId;
    private String attendeeId;
    private LocalDateTime baseTime;
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        baseTime = LocalDateTime.now().plusDays(1);
        
        // Create valid organizer and attendee users
        String organizerUsername = "testorg" + System.currentTimeMillis();
        String attendeeUsername = "testatt" + System.currentTimeMillis();
        
        // Register organizer
        eventHub.registerUser(organizerUsername, organizerUsername + "@test.com", "password123", 
                             "Test", "Organizer", com.campuseventhub.model.user.UserRole.ORGANIZER);
        
        // Register attendee  
        eventHub.registerUser(attendeeUsername, attendeeUsername + "@test.com", "password123",
                             "Test", "Attendee", com.campuseventhub.model.user.UserRole.ATTENDEE);
        
        // Authenticate to get user IDs
        var organizer = eventHub.authenticateUser(organizerUsername, "password123");
        var attendee = eventHub.authenticateUser(attendeeUsername, "password123");
        
        organizerId = organizer.getUserId();
        attendeeId = attendee.getUserId();
    }
    
    @Test
    @DisplayName("Organizer should be able to create multiple events at same time in different venues")
    void organizerCanCreateSimultaneousEventsInDifferentVenues() {
        // Create first event
        Event event1 = eventHub.createEvent(
            "Workshop A", "First workshop", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 30
        );
        assertNotNull(event1, "First event should be created successfully");
        
        // Create second event at same time (should succeed)
        Event event2 = eventHub.createEvent(
            "Workshop B", "Second workshop", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 30
        );
        assertNotNull(event2, "Second simultaneous event should be created successfully");
        
        // Verify both events exist and have overlapping times
        assertTrue(event1.getStartDateTime().equals(event2.getStartDateTime()));
        assertTrue(event1.getEndDateTime().equals(event2.getEndDateTime()));
        assertEquals(organizerId, event1.getOrganizerId());
        assertEquals(organizerId, event2.getOrganizerId());
    }
    
    @Test
    @DisplayName("Attendee should be blocked from registering for overlapping events")
    void attendeeShouldBeBlockedFromOverlappingEvents() {
        // Create two overlapping events
        Event event1 = eventHub.createEvent(
            "Event A", "First event", EventType.GUEST_LECTURE,
            baseTime, baseTime.plusHours(3), organizerId, null, 50
        );
        
        Event event2 = eventHub.createEvent(
            "Event B", "Second event", EventType.SEMINAR,
            baseTime.plusHours(1), baseTime.plusHours(4), organizerId, null, 50
        );
        
        assertNotNull(event1);
        assertNotNull(event2);
        
        // Register for first event (should succeed)
        Registration registration1 = eventHub.registerForEvent(attendeeId, event1.getEventId());
        assertNotNull(registration1, "First registration should succeed");
        
        // Try to register for overlapping event (should fail)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventHub.registerForEvent(attendeeId, event2.getEventId());
        }, "Registration for overlapping event should be blocked");
        
        assertTrue(exception.getMessage().contains("Schedule conflict"), 
                  "Error message should mention schedule conflict");
    }
    
    @Test
    @DisplayName("Attendee can register for non-overlapping events")
    void attendeeCanRegisterForNonOverlappingEvents() {
        // Create two non-overlapping events
        Event event1 = eventHub.createEvent(
            "Morning Event", "First event", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 30
        );
        
        Event event2 = eventHub.createEvent(
            "Afternoon Event", "Second event", EventType.SEMINAR,
            baseTime.plusHours(3), baseTime.plusHours(5), organizerId, null, 30
        );
        
        assertNotNull(event1);
        assertNotNull(event2);
        
        // Register for first event
        Registration registration1 = eventHub.registerForEvent(attendeeId, event1.getEventId());
        assertNotNull(registration1, "First registration should succeed");
        
        // Register for non-overlapping event (should succeed)
        Registration registration2 = eventHub.registerForEvent(attendeeId, event2.getEventId());
        assertNotNull(registration2, "Second registration should succeed");
        
        assertNotEquals(registration1.getRegistrationId(), registration2.getRegistrationId());
    }
    
    @Test
    @DisplayName("Attendee can register for event after cancelling conflicting registration")
    void attendeeCanRegisterAfterCancellingConflictingRegistration() {
        // Create two overlapping events
        Event event1 = eventHub.createEvent(
            "Event A", "First event", EventType.CLUB_MEETING,
            baseTime, baseTime.plusHours(2), organizerId, null, 50
        );
        
        Event event2 = eventHub.createEvent(
            "Event B", "Second event", EventType.WORKSHOP,
            baseTime.plusMinutes(30), baseTime.plusHours(3), organizerId, null, 30
        );
        
        // Register for first event
        Registration registration1 = eventHub.registerForEvent(attendeeId, event1.getEventId());
        assertNotNull(registration1);
        
        // Try to register for overlapping event (should fail)
        assertThrows(IllegalArgumentException.class, () -> {
            eventHub.registerForEvent(attendeeId, event2.getEventId());
        });
        
        // Cancel first registration
        boolean cancelled = eventHub.cancelEventRegistration(registration1.getRegistrationId(), "Changed mind");
        assertTrue(cancelled, "Cancellation should succeed");
        
        // Now should be able to register for second event
        Registration registration2 = eventHub.registerForEvent(attendeeId, event2.getEventId());
        assertNotNull(registration2, "Registration should succeed after cancelling conflicting event");
    }
}