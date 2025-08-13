package com.campuseventhub.model.event;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

/**
 * Test class for the fixed event capacity tracking functionality.
 * 
 * Tests the bug fixes for:
 * - hasCapacity() correctly tracking confirmed registrations
 * - getAvailableSpots() returning accurate counts
 * - Proper handling of waitlisted vs confirmed registrations
 */
class EventCapacityFixTest {
    
    private Event event;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusHours(2);
        event = new Event("Test Event", "Test Description", EventType.WORKSHOP, 
                          startTime, endTime, "organizer123");
        event.setMaxCapacity(3);
        event.setRegistrationDeadline(startTime.minusHours(1));
        event.setStatus(EventStatus.PUBLISHED);
    }
    
    @Test
    @DisplayName("Should correctly track capacity with confirmed registrations")
    void testHasCapacityWithConfirmedRegistrations() {
        // Initially should have capacity
        assertTrue(event.hasCapacity());
        assertEquals(3, event.getAvailableSpots());
        
        // Add first confirmed registration
        Registration reg1 = event.addRegistration("attendee1");
        assertNotNull(reg1);
        assertEquals(RegistrationStatus.CONFIRMED, reg1.getStatus());
        assertTrue(event.hasCapacity());
        assertEquals(2, event.getAvailableSpots());
        
        // Add second confirmed registration
        Registration reg2 = event.addRegistration("attendee2");
        assertNotNull(reg2);
        assertEquals(RegistrationStatus.CONFIRMED, reg2.getStatus());
        assertTrue(event.hasCapacity());
        assertEquals(1, event.getAvailableSpots());
        
        // Add third confirmed registration (full capacity)
        Registration reg3 = event.addRegistration("attendee3");
        assertNotNull(reg3);
        assertEquals(RegistrationStatus.CONFIRMED, reg3.getStatus());
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
    }
    
    @Test
    @DisplayName("Should handle waitlisted registrations correctly")
    void testCapacityWithWaitlistedRegistrations() {
        // Fill to capacity
        event.addRegistration("attendee1");
        event.addRegistration("attendee2"); 
        event.addRegistration("attendee3");
        
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
        
        // Fourth registration should go to waitlist
        Registration reg4 = event.addRegistration("attendee4");
        assertNotNull(reg4);
        assertEquals(RegistrationStatus.WAITLISTED, reg4.getStatus());
        
        // Capacity should still be full
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
        
        // Add another waitlisted registration
        Registration reg5 = event.addRegistration("attendee5");
        assertNotNull(reg5);
        assertEquals(RegistrationStatus.WAITLISTED, reg5.getStatus());
        assertEquals(2, reg5.getWaitlistPosition());
        
        // Still no capacity
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
    }
    
    @Test
    @DisplayName("Should handle capacity after registration removal")
    void testCapacityAfterRegistrationRemoval() {
        // Fill to capacity
        Registration reg1 = event.addRegistration("attendee1");
        Registration reg2 = event.addRegistration("attendee2");
        Registration reg3 = event.addRegistration("attendee3");
        
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
        
        // Remove one registration
        boolean removed = event.removeRegistration(reg2.getRegistrationId());
        assertTrue(removed);
        
        // Should now have capacity
        assertTrue(event.hasCapacity());
        assertEquals(1, event.getAvailableSpots());
        
        // Add new registration
        Registration reg4 = event.addRegistration("attendee4");
        assertNotNull(reg4);
        assertEquals(RegistrationStatus.CONFIRMED, reg4.getStatus());
        
        // Back to full capacity
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
    }
    
    @Test
    @DisplayName("Should handle mixed confirmed and waitlisted registrations")
    void testMixedRegistrationStatuses() {
        // Add registrations to fill capacity
        Registration reg1 = event.addRegistration("attendee1");
        Registration reg2 = event.addRegistration("attendee2");
        Registration reg3 = event.addRegistration("attendee3");
        Registration reg4 = event.addRegistration("attendee4"); // Should be waitlisted
        
        // Verify confirmed vs waitlisted
        assertEquals(RegistrationStatus.CONFIRMED, reg1.getStatus());
        assertEquals(RegistrationStatus.CONFIRMED, reg2.getStatus());
        assertEquals(RegistrationStatus.CONFIRMED, reg3.getStatus());
        assertEquals(RegistrationStatus.WAITLISTED, reg4.getStatus());
        
        // Capacity calculations should only count confirmed
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
        
        // Total registrations vs confirmed registrations
        assertEquals(3, event.getRegistrations().size()); // Only confirmed
        assertEquals(1, event.getWaitlistSize()); // Waitlisted
    }
    
    @Test
    @DisplayName("Should handle capacity increase scenario")
    void testCapacityIncrease() {
        // Fill to capacity
        event.addRegistration("attendee1");
        event.addRegistration("attendee2");
        event.addRegistration("attendee3");
        event.addRegistration("attendee4"); // Waitlisted
        
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
        assertEquals(1, event.getWaitlistSize());
        
        // Increase capacity
        event.setMaxCapacity(5);
        
        // Should now have available spots
        assertTrue(event.hasCapacity());
        assertEquals(2, event.getAvailableSpots());
        
        // Waitlist should still exist
        assertEquals(1, event.getWaitlistSize());
    }
    
    @Test
    @DisplayName("Should handle zero capacity edge case")
    void testZeroCapacity() {
        event.setMaxCapacity(0);
        
        assertFalse(event.hasCapacity());
        assertEquals(0, event.getAvailableSpots());
        
        // Registration should be waitlisted
        Registration reg = event.addRegistration("attendee1");
        assertNotNull(reg);
        assertEquals(RegistrationStatus.WAITLISTED, reg.getStatus());
    }
}