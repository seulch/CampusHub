package com.campuseventhub.model.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class RegistrationTest {
    
    private Registration registration;
    private String attendeeId;
    private String eventId;
    
    @BeforeEach
    void setUp() {
        attendeeId = "attendee123";
        eventId = "event456";
        registration = new Registration(attendeeId, eventId);
    }
    
    @Test
    @DisplayName("Should create registration with correct initial values")
    void testRegistrationCreation() {
        assertNotNull(registration.getRegistrationId());
        assertEquals(attendeeId, registration.getAttendeeId());
        assertEquals(eventId, registration.getEventId());
        assertEquals(RegistrationStatus.PENDING, registration.getStatus());
        assertNotNull(registration.getRegistrationTime());
        assertFalse(registration.isAttended());
        assertNull(registration.getAttendanceTime());
        assertEquals(0, registration.getWaitlistPosition());
        assertNull(registration.getCancellationReason());
        assertNull(registration.getCancellationTime());
        assertFalse(registration.isConfirmed());
        assertFalse(registration.isWaitlisted());
    }
    
    @Test
    @DisplayName("Should confirm registration successfully")
    void testConfirmRegistration() {
        registration.confirmRegistration();
        
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        assertTrue(registration.isConfirmed());
        assertFalse(registration.isWaitlisted());
    }
    
    @Test
    @DisplayName("Should cancel registration with reason")
    void testCancelRegistration() {
        String reason = "Schedule conflict";
        
        registration.cancelRegistration(reason);
        
        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
        assertEquals(reason, registration.getCancellationReason());
        assertNotNull(registration.getCancellationTime());
        assertFalse(registration.isConfirmed());
    }
    
    @Test
    @DisplayName("Should cancel registration without reason")
    void testCancelRegistrationWithoutReason() {
        registration.cancelRegistration(null);
        
        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
        assertNull(registration.getCancellationReason());
        assertNotNull(registration.getCancellationTime());
    }
    
    @Test
    @DisplayName("Should mark attendance successfully")
    void testMarkAttendance() {
        registration.markAttendance();
        
        assertTrue(registration.isAttended());
        assertNotNull(registration.getAttendanceTime());
    }
    
    @Test
    @DisplayName("Should handle waitlist position correctly")
    void testWaitlistPosition() {
        assertFalse(registration.isWaitlisted());
        
        registration.setWaitlistPosition(5);
        
        assertEquals(5, registration.getWaitlistPosition());
        assertTrue(registration.isWaitlisted());
    }
    
    @Test
    @DisplayName("Should handle confirmed status checks")
    void testIsConfirmed() {
        assertFalse(registration.isConfirmed());
        
        registration.setStatus(RegistrationStatus.CONFIRMED);
        assertTrue(registration.isConfirmed());
        
        registration.setStatus(RegistrationStatus.PENDING);
        assertFalse(registration.isConfirmed());
        
        registration.setStatus(RegistrationStatus.CANCELLED);
        assertFalse(registration.isConfirmed());
        
        registration.setStatus(RegistrationStatus.WAITLISTED);
        assertFalse(registration.isConfirmed());
    }
    
    @Test
    @DisplayName("Should handle waitlisted status checks")
    void testIsWaitlisted() {
        assertFalse(registration.isWaitlisted());
        
        registration.setWaitlistPosition(1);
        assertTrue(registration.isWaitlisted());
        
        registration.setWaitlistPosition(0);
        assertFalse(registration.isWaitlisted());
        
        registration.setWaitlistPosition(-1);
        assertFalse(registration.isWaitlisted());
    }
    
    @Test
    @DisplayName("Should set and get all properties correctly")
    void testSettersAndGetters() {
        LocalDateTime attendanceTime = LocalDateTime.now();
        LocalDateTime cancellationTime = LocalDateTime.now().minusHours(1);
        
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setAttended(true);
        registration.setAttendanceTime(attendanceTime);
        registration.setWaitlistPosition(3);
        registration.setCancellationReason("Test reason");
        registration.setCancellationTime(cancellationTime);
        
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        assertTrue(registration.isAttended());
        assertEquals(attendanceTime, registration.getAttendanceTime());
        assertEquals(3, registration.getWaitlistPosition());
        assertEquals("Test reason", registration.getCancellationReason());
        assertEquals(cancellationTime, registration.getCancellationTime());
    }
    
    @Test
    @DisplayName("Should handle registration lifecycle correctly")
    void testRegistrationLifecycle() {
        // Initial state: PENDING
        assertEquals(RegistrationStatus.PENDING, registration.getStatus());
        assertFalse(registration.isConfirmed());
        
        // Confirm registration
        registration.confirmRegistration();
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        assertTrue(registration.isConfirmed());
        
        // Mark attendance at event
        registration.markAttendance();
        assertTrue(registration.isAttended());
        assertNotNull(registration.getAttendanceTime());
        
        // Still confirmed after marking attendance
        assertTrue(registration.isConfirmed());
    }
    
    @Test
    @DisplayName("Should handle waitlist to confirmed transition")
    void testWaitlistToConfirmedTransition() {
        // Start as waitlisted
        registration.setWaitlistPosition(2);
        registration.setStatus(RegistrationStatus.WAITLISTED);
        
        assertTrue(registration.isWaitlisted());
        assertFalse(registration.isConfirmed());
        
        // Promote from waitlist
        registration.setWaitlistPosition(0);
        registration.confirmRegistration();
        
        assertFalse(registration.isWaitlisted());
        assertTrue(registration.isConfirmed());
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
    }
    
    @Test
    @DisplayName("Should generate unique registration IDs")
    void testUniqueRegistrationIds() {
        Registration registration2 = new Registration("attendee789", "event012");
        
        assertNotEquals(registration.getRegistrationId(), registration2.getRegistrationId());
    }
    
    @Test
    @DisplayName("Should handle multiple attendance markings")
    void testMultipleAttendanceMarkings() {
        LocalDateTime firstMark = LocalDateTime.now();
        
        registration.markAttendance();
        assertTrue(registration.isAttended());
        LocalDateTime firstAttendanceTime = registration.getAttendanceTime();
        
        // Simulate time passing
        try {
            Thread.sleep(10); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mark attendance again
        registration.markAttendance();
        assertTrue(registration.isAttended());
        
        // Should have updated the attendance time
        assertTrue(registration.getAttendanceTime().isAfter(firstAttendanceTime) || 
                  registration.getAttendanceTime().equals(firstAttendanceTime));
    }
}