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
 * Test duplicate registration prevention in RegistrationManager
 */
class DuplicateRegistrationTest {
    
    private RegistrationManager registrationManager;
    private String testEventId;
    private String testAttendeeId;
    
    @BeforeEach
    void setUp() {
        registrationManager = new RegistrationManager();
        testEventId = "test-event-" + java.util.UUID.randomUUID().toString();
        testAttendeeId = "test-attendee-" + java.util.UUID.randomUUID().toString();
    }
    
    @Test
    @DisplayName("Should prevent duplicate registrations for same attendee and event")
    void shouldPreventDuplicateRegistrations() {
        // First registration should succeed
        Registration firstRegistration = registrationManager.createRegistration(testEventId, testAttendeeId);
        assertNotNull(firstRegistration, "First registration should succeed");
        assertEquals(testEventId, firstRegistration.getEventId());
        assertEquals(testAttendeeId, firstRegistration.getAttendeeId());
        
        // Second registration for same attendee and event should fail
        Registration duplicateRegistration = registrationManager.createRegistration(testEventId, testAttendeeId);
        assertNull(duplicateRegistration, "Duplicate registration should be prevented");
    }
    
    @Test
    @DisplayName("Should allow registration for different attendees on same event")
    void shouldAllowDifferentAttendeesOnSameEvent() {
        String secondAttendeeId = "test-attendee-789";
        
        Registration firstRegistration = registrationManager.createRegistration(testEventId, testAttendeeId);
        Registration secondRegistration = registrationManager.createRegistration(testEventId, secondAttendeeId);
        
        assertNotNull(firstRegistration, "First registration should succeed");
        assertNotNull(secondRegistration, "Different attendee registration should succeed");
        assertNotEquals(firstRegistration.getRegistrationId(), secondRegistration.getRegistrationId());
    }
    
    @Test
    @DisplayName("Should allow same attendee to register for different events")
    void shouldAllowSameAttendeeForDifferentEvents() {
        String secondEventId = "test-event-789";
        
        Registration firstRegistration = registrationManager.createRegistration(testEventId, testAttendeeId);
        Registration secondRegistration = registrationManager.createRegistration(secondEventId, testAttendeeId);
        
        assertNotNull(firstRegistration, "First registration should succeed");
        assertNotNull(secondRegistration, "Registration for different event should succeed");
        assertNotEquals(firstRegistration.getRegistrationId(), secondRegistration.getRegistrationId());
    }
    
    @Test
    @DisplayName("Should allow re-registration after cancellation")
    void shouldAllowReRegistrationAfterCancellation() {
        // First registration
        Registration firstRegistration = registrationManager.createRegistration(testEventId, testAttendeeId);
        assertNotNull(firstRegistration, "First registration should succeed");
        
        // Cancel the registration
        boolean cancelled = registrationManager.cancelRegistration(firstRegistration.getRegistrationId());
        assertTrue(cancelled, "Registration cancellation should succeed");
        
        // Should be able to register again after cancellation
        Registration newRegistration = registrationManager.createRegistration(testEventId, testAttendeeId);
        assertNotNull(newRegistration, "Re-registration after cancellation should succeed");
        assertNotEquals(firstRegistration.getRegistrationId(), newRegistration.getRegistrationId());
    }
}