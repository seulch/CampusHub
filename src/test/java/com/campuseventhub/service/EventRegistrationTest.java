package com.campuseventhub.service;

import com.campuseventhub.model.event.*;
import com.campuseventhub.model.user.*;
import com.campuseventhub.persistence.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test class for event registration system and conflict detection
 */
public class EventRegistrationTest {
    
    private EventManager eventManager;
    private UserManager userManager;
    private String organizerId;
    private String attendeeId1;
    private String attendeeId2;
    private String venueId;
    
    @BeforeEach
    public void setUp() {
        // Clean up any existing test data
        cleanup();
        
        eventManager = new EventManager();
        userManager = new UserManager();
        
        // Create test users
        User organizer = userManager.createUser("testorg", "org@test.com", "password123", 
                                               "Test", "Organizer", UserRole.ORGANIZER);
        User attendee1 = userManager.createUser("attendee1", "att1@test.com", "password123", 
                                               "John", "Doe", UserRole.ATTENDEE);
        User attendee2 = userManager.createUser("attendee2", "att2@test.com", "password123", 
                                               "Jane", "Smith", UserRole.ATTENDEE);
        
        organizerId = organizer.getUserId();
        attendeeId1 = attendee1.getUserId();
        attendeeId2 = attendee2.getUserId();
        venueId = "TEST_VENUE_001";
    }
    
    @AfterEach
    public void cleanup() {
        // Clean up test files
        DataManager.deleteDataFile("events.ser");
        DataManager.deleteDataFile("registrations.ser");
        DataManager.deleteDataFile("users.ser");
    }
    
    @Test
    public void testCreateEventWithValidation() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventManager.createEvent("Test Workshop", "A test workshop", 
                                             EventType.WORKSHOP, startTime, endTime, 
                                             organizerId, venueId, 30);
        
        assertNotNull(event);
        assertEquals("Test Workshop", event.getTitle());
        assertEquals(EventType.WORKSHOP, event.getEventType());
        assertEquals(organizerId, event.getOrganizerId());
    }
    
    @Test
    public void testEventValidationFailures() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Invalid title
        assertThrows(IllegalArgumentException.class, () -> {
            eventManager.createEvent("Test", "Description", EventType.WORKSHOP, 
                                   startTime, endTime, organizerId, venueId, 30);
        });
        
        // Invalid duration (too short)
        assertThrows(IllegalArgumentException.class, () -> {
            eventManager.createEvent("Valid Title", "Description", EventType.WORKSHOP, 
                                   startTime, startTime.plusMinutes(5), organizerId, venueId, 30);
        });
        
        // Invalid organizer
        assertThrows(IllegalArgumentException.class, () -> {
            eventManager.createEvent("Valid Title", "Description", EventType.WORKSHOP, 
                                   startTime, endTime, "", venueId, 30);
        });
    }
    
    @Test
    public void testEventCreationWithoutConflicts() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Create first event
        Event event1 = eventManager.createEvent("First Event", "First event description", 
                                               EventType.WORKSHOP, startTime, endTime, 
                                               organizerId, venueId, 30);
        
        // Create non-overlapping event with same organizer
        LocalDateTime laterStart = endTime.plusMinutes(30);
        LocalDateTime laterEnd = laterStart.plusHours(2);
        
        Event event2 = eventManager.createEvent("Second Event", "Second event description", 
                                               EventType.SEMINAR, laterStart, laterEnd, 
                                               organizerId, venueId, 30);
        
        assertNotNull(event1);
        assertNotNull(event2);
    }
    
    @Test
    public void testOrganizerConflictDetection() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Create first event
        eventManager.createEvent("First Event", "First event description", 
                                EventType.WORKSHOP, startTime, endTime, 
                                organizerId, venueId, 30);
        
        // Try to create overlapping event with same organizer
        LocalDateTime overlappingStart = startTime.plusMinutes(30);
        LocalDateTime overlappingEnd = endTime.plusMinutes(30);
        
        assertThrows(IllegalArgumentException.class, () -> {
            eventManager.createEvent("Second Event", "Second event description", 
                                   EventType.SEMINAR, overlappingStart, overlappingEnd, 
                                   organizerId, venueId, 30);
        });
    }
    
    @Test
    public void testSuccessfulEventRegistration() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventManager.createEvent("Test Workshop", "A test workshop", 
                                             EventType.WORKSHOP, startTime, endTime, 
                                             organizerId, venueId, 30);
        event.setMaxCapacity(10);
        
        Registration registration = eventManager.registerAttendeeForEvent(attendeeId1, event.getEventId());
        
        assertNotNull(registration);
        assertEquals(attendeeId1, registration.getAttendeeId());
        assertEquals(event.getEventId(), registration.getEventId());
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        assertFalse(registration.isWaitlisted());
        
        // Verify registration counts
        assertEquals(1, eventManager.getCurrentRegistrationCount(event.getEventId()));
        assertEquals(0, eventManager.getWaitlistSize(event.getEventId()));
    }
    
    @Test
    public void testCapacityManagementAndWaitlist() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventManager.createEvent("Test Workshop", "A test workshop", 
                                             EventType.WORKSHOP, startTime, endTime, 
                                             organizerId, venueId, 30);
        event.setMaxCapacity(1); // Very small capacity for testing
        
        // First registration should be confirmed
        Registration reg1 = eventManager.registerAttendeeForEvent(attendeeId1, event.getEventId());
        assertEquals(RegistrationStatus.CONFIRMED, reg1.getStatus());
        assertFalse(reg1.isWaitlisted());
        
        // Second registration should be waitlisted
        Registration reg2 = eventManager.registerAttendeeForEvent(attendeeId2, event.getEventId());
        assertEquals(RegistrationStatus.WAITLISTED, reg2.getStatus());
        assertTrue(reg2.isWaitlisted());
        assertEquals(1, reg2.getWaitlistPosition());
        
        // Verify counts
        assertEquals(1, eventManager.getCurrentRegistrationCount(event.getEventId()));
        assertEquals(1, eventManager.getWaitlistSize(event.getEventId()));
    }
    
    @Test
    public void testRegistrationCancellationAndWaitlistPromotion() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventManager.createEvent("Test Workshop", "A test workshop", 
                                             EventType.WORKSHOP, startTime, endTime, 
                                             organizerId, venueId, 30);
        event.setMaxCapacity(1);
        
        // Register two attendees (second goes to waitlist)
        Registration reg1 = eventManager.registerAttendeeForEvent(attendeeId1, event.getEventId());
        Registration reg2 = eventManager.registerAttendeeForEvent(attendeeId2, event.getEventId());
        
        assertEquals(RegistrationStatus.CONFIRMED, reg1.getStatus());
        assertEquals(RegistrationStatus.WAITLISTED, reg2.getStatus());
        
        // Cancel first registration
        boolean cancelled = eventManager.cancelRegistration(reg1.getRegistrationId(), "Changed mind");
        assertTrue(cancelled);
        assertEquals(RegistrationStatus.CANCELLED, reg1.getStatus());
        
        // Second registration should be promoted
        assertEquals(RegistrationStatus.CONFIRMED, reg2.getStatus());
        assertEquals(0, reg2.getWaitlistPosition());
        
        // Verify counts
        assertEquals(1, eventManager.getCurrentRegistrationCount(event.getEventId()));
        assertEquals(0, eventManager.getWaitlistSize(event.getEventId()));
    }
    
    @Test
    public void testDuplicateRegistrationPrevention() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventManager.createEvent("Test Workshop", "A test workshop", 
                                             EventType.WORKSHOP, startTime, endTime, 
                                             organizerId, venueId, 30);
        
        // First registration should succeed
        Registration reg1 = eventManager.registerAttendeeForEvent(attendeeId1, event.getEventId());
        assertNotNull(reg1);
        
        // Second registration for same attendee should fail
        assertThrows(IllegalArgumentException.class, () -> {
            eventManager.registerAttendeeForEvent(attendeeId1, event.getEventId());
        });
    }
    
    @Test
    public void testRegistrationForNonExistentEvent() {
        assertThrows(IllegalArgumentException.class, () -> {
            eventManager.registerAttendeeForEvent(attendeeId1, "NON_EXISTENT_EVENT");
        });
    }
    
    @Test
    public void testGetRegistrationLists() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventManager.createEvent("Test Workshop", "A test workshop", 
                                             EventType.WORKSHOP, startTime, endTime, 
                                             organizerId, venueId, 30);
        
        Registration reg1 = eventManager.registerAttendeeForEvent(attendeeId1, event.getEventId());
        Registration reg2 = eventManager.registerAttendeeForEvent(attendeeId2, event.getEventId());
        
        // Test get registrations by event
        List<Registration> eventRegistrations = eventManager.getEventRegistrations(event.getEventId());
        assertEquals(2, eventRegistrations.size());
        assertTrue(eventRegistrations.contains(reg1));
        assertTrue(eventRegistrations.contains(reg2));
        
        // Test get registrations by attendee
        List<Registration> attendee1Registrations = eventManager.getAttendeeRegistrations(attendeeId1);
        assertEquals(1, attendee1Registrations.size());
        assertEquals(reg1, attendee1Registrations.get(0));
        
        List<Registration> attendee2Registrations = eventManager.getAttendeeRegistrations(attendeeId2);
        assertEquals(1, attendee2Registrations.size());
        assertEquals(reg2, attendee2Registrations.get(0));
    }
    
    @Test
    public void testNonOverlappingEventsAllowed() {
        LocalDateTime startTime1 = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime1 = startTime1.plusHours(2);
        
        LocalDateTime startTime2 = endTime1.plusMinutes(30); // Start after first event ends
        LocalDateTime endTime2 = startTime2.plusHours(2);
        
        // Both events should be created successfully
        Event event1 = eventManager.createEvent("First Event", "First event description", 
                                               EventType.WORKSHOP, startTime1, endTime1, 
                                               organizerId, venueId, 30);
        
        Event event2 = eventManager.createEvent("Second Event", "Second event description", 
                                               EventType.SEMINAR, startTime2, endTime2, 
                                               organizerId, venueId, 30);
        
        assertNotNull(event1);
        assertNotNull(event2);
        
        // Verify that both events are properly managed
        List<Event> organizerEvents = eventManager.getEventsByOrganizer(organizerId);
        assertEquals(2, organizerEvents.size());
    }
}