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
        assertFalse(registration.isAttended());
        assertEquals(0, registration.getWaitlistPosition());
        assertNotNull(registration.getRegistrationTime());
    }
    
    @Test
    @DisplayName("Should confirm registration successfully")
    void testConfirmRegistration() {
        registration.confirmRegistration();
        
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        assertTrue(registration.isConfirmed());
    }
    
    @Test
    @DisplayName("Should cancel registration with reason")
    void testCancelRegistration() {
        String reason = "Schedule conflict";
        registration.cancelRegistration(reason);
        
        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
        assertEquals(reason, registration.getCancellationReason());
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
    @DisplayName("Should check waitlist status correctly")
    void testIsWaitlisted() {
        // Initially not waitlisted
        assertFalse(registration.isWaitlisted());
        
        // Set waitlist position
        registration.setWaitlistPosition(1);
        assertTrue(registration.isWaitlisted());
        
        registration.setWaitlistPosition(0);
        assertFalse(registration.isWaitlisted());
    }
    
    @Test
    @DisplayName("Should handle waitlist position updates")
    void testWaitlistPositionUpdates() {
        registration.setWaitlistPosition(5);
        assertEquals(5, registration.getWaitlistPosition());
        
        registration.setWaitlistPosition(1);
        assertEquals(1, registration.getWaitlistPosition());
    }
    
    @Test
    @DisplayName("Should handle status updates")
    void testStatusUpdates() {
        registration.setStatus(RegistrationStatus.CONFIRMED);
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        
        registration.setStatus(RegistrationStatus.CANCELLED);
        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
    }
    
    @Test
    @DisplayName("Should handle attendance updates")
    void testAttendanceUpdates() {
        registration.setAttended(true);
        assertTrue(registration.isAttended());
        
        registration.setAttended(false);
        assertFalse(registration.isAttended());
    }
    
    @Test
    @DisplayName("Should handle attendance time updates")
    void testAttendanceTimeUpdates() {
        LocalDateTime attendanceTime = LocalDateTime.now();
        registration.setAttendanceTime(attendanceTime);
        
        assertEquals(attendanceTime, registration.getAttendanceTime());
    }
    
    @Test
    @DisplayName("Should handle cancellation reason updates")
    void testCancellationReasonUpdates() {
        String reason = "Changed plans";
        registration.setCancellationReason(reason);
        
        assertEquals(reason, registration.getCancellationReason());
    }
    
    @Test
    @DisplayName("Should handle cancellation time updates")
    void testCancellationTimeUpdates() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        registration.setCancellationTime(cancellationTime);
        
        assertEquals(cancellationTime, registration.getCancellationTime());
    }
    
    @Test
    @DisplayName("Should generate unique registration IDs")
    void testUniqueRegistrationIds() {
        Registration reg1 = new Registration("attendee1", "event1");
        Registration reg2 = new Registration("attendee2", "event2");
        
        assertNotNull(reg1.getRegistrationId());
        assertNotNull(reg2.getRegistrationId());
        assertNotEquals(reg1.getRegistrationId(), reg2.getRegistrationId());
    }
    
    @Test
    @DisplayName("Should handle registration time correctly")
    void testRegistrationTime() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        Registration newReg = new Registration("attendee", "event");
        LocalDateTime afterCreation = LocalDateTime.now();
        
        assertTrue(newReg.getRegistrationTime().isAfter(beforeCreation) || 
                  newReg.getRegistrationTime().isEqual(beforeCreation));
        assertTrue(newReg.getRegistrationTime().isBefore(afterCreation) || 
                  newReg.getRegistrationTime().isEqual(afterCreation));
    }
} 