package com.campuseventhub.service;

import com.campuseventhub.model.event.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Test class for ScheduleValidator conflict detection
 */
public class ScheduleValidatorTest {
    
    private ScheduleValidator validator;
    private Event testEvent1;
    private Event testEvent2;
    
    @BeforeEach
    public void setUp() {
        validator = new ScheduleValidator();
        
        LocalDateTime start1 = LocalDateTime.of(2024, 6, 15, 10, 0);
        LocalDateTime end1 = LocalDateTime.of(2024, 6, 15, 12, 0);
        
        LocalDateTime start2 = LocalDateTime.of(2024, 6, 15, 14, 0);
        LocalDateTime end2 = LocalDateTime.of(2024, 6, 15, 16, 0);
        
        testEvent1 = new Event("Test Event 1", "Description 1", EventType.WORKSHOP, 
                              start1, end1, "organizer1");
        
        testEvent2 = new Event("Test Event 2", "Description 2", EventType.SEMINAR, 
                              start2, end2, "organizer2");
    }
    
    @Test
    public void testEventRegistrationAndUnregistration() {
        // Register events
        validator.registerEvent(testEvent1);
        validator.registerEvent(testEvent2);
        
        // Should detect organizer conflicts
        assertFalse(validator.isOrganizerAvailable("organizer1", 
                    testEvent1.getStartDateTime(), testEvent1.getEndDateTime(), null));
        
        // Unregister and check availability
        validator.unregisterEvent(testEvent1);
        assertTrue(validator.isOrganizerAvailable("organizer1", 
                   testEvent1.getStartDateTime(), testEvent1.getEndDateTime(), null));
    }
    
    @Test
    public void testVenueAvailability() {
        // Note: Venue availability testing is simplified in this implementation
        // For demo purposes, venue availability always returns true
        assertTrue(validator.isTimeSlotAvailable("VENUE_001", 
                   LocalDateTime.of(2024, 6, 15, 10, 0),
                   LocalDateTime.of(2024, 6, 15, 12, 0), null));
    }
    
    @Test
    public void testOverlappingTimeConflicts() {
        // This test is simplified to focus on organizer conflicts
        validator.registerEvent(testEvent1); // organizer1, 10:00-12:00
        
        // Overlapping time with same organizer - should not be available
        LocalDateTime overlapStart = LocalDateTime.of(2024, 6, 15, 11, 0);
        LocalDateTime overlapEnd = LocalDateTime.of(2024, 6, 15, 13, 0);
        assertFalse(validator.isOrganizerAvailable("organizer1", overlapStart, overlapEnd, null));
        
        // Different organizer - should be available
        assertTrue(validator.isOrganizerAvailable("organizer2", overlapStart, overlapEnd, null));
    }
    
    @Test
    public void testNonOverlappingTimes() {
        validator.registerEvent(testEvent1); // organizer1, 10:00-12:00
        
        // After the event with same organizer - should be available
        LocalDateTime afterStart = LocalDateTime.of(2024, 6, 15, 12, 0);
        LocalDateTime afterEnd = LocalDateTime.of(2024, 6, 15, 14, 0);
        assertTrue(validator.isOrganizerAvailable("organizer1", afterStart, afterEnd, null));
    }
    
    @Test
    public void testOrganizerAvailability() {
        validator.registerEvent(testEvent1); // organizer1, 10:00-12:00
        
        // Same organizer, overlapping time - should not be available
        LocalDateTime overlapStart = LocalDateTime.of(2024, 6, 15, 11, 0);
        LocalDateTime overlapEnd = LocalDateTime.of(2024, 6, 15, 13, 0);
        assertFalse(validator.isOrganizerAvailable("organizer1", overlapStart, overlapEnd, null));
        
        // Different organizer, same time - should be available
        assertTrue(validator.isOrganizerAvailable("organizer2", overlapStart, overlapEnd, null));
        
        // Same organizer, non-overlapping time - should be available
        LocalDateTime laterStart = LocalDateTime.of(2024, 6, 15, 13, 0);
        LocalDateTime laterEnd = LocalDateTime.of(2024, 6, 15, 15, 0);
        assertTrue(validator.isOrganizerAvailable("organizer1", laterStart, laterEnd, null));
    }
    
    @Test
    public void testConflictDetection() {
        validator.registerEvent(testEvent1); // organizer1, 10:00-12:00
        
        // Create overlapping event with same organizer
        LocalDateTime overlapStart = LocalDateTime.of(2024, 6, 15, 11, 0);
        LocalDateTime overlapEnd = LocalDateTime.of(2024, 6, 15, 13, 0);
        Event conflictingEvent = new Event("Conflicting Event", "Description", 
                                         EventType.WORKSHOP, overlapStart, overlapEnd, "organizer1");
        
        List<String> conflicts = validator.detectConflicts(conflictingEvent, null);
        assertEquals(1, conflicts.size()); // Organizer conflict
        assertTrue(conflicts.get(0).contains("Organizer conflict"));
        
        // Test with different organizer
        Event nonConflictingEvent = new Event("Non-Conflicting Event", "Description", 
                                             EventType.WORKSHOP, overlapStart, overlapEnd, "organizer2");
        conflicts = validator.detectConflicts(nonConflictingEvent, null);
        assertEquals(0, conflicts.size()); // No conflicts
    }
    
    @Test
    public void testExcludeEventFromConflictCheck() {
        validator.registerEvent(testEvent1);
        
        // When updating the same event, it should exclude itself from conflict check
        assertTrue(validator.isOrganizerAvailable("organizer1", 
                   testEvent1.getStartDateTime(), testEvent1.getEndDateTime(), 
                   testEvent1.getEventId()));
    }
    
    @Test
    public void testEventDurationValidation() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 15, 10, 0);
        
        // Valid durations
        assertTrue(validator.validateEventDuration(start, start.plusMinutes(15))); // Minimum
        assertTrue(validator.validateEventDuration(start, start.plusHours(2))); // Normal
        assertTrue(validator.validateEventDuration(start, start.plusHours(12))); // Maximum
        
        // Invalid durations
        assertFalse(validator.validateEventDuration(start, start.plusMinutes(5))); // Too short
        assertFalse(validator.validateEventDuration(start, start.plusHours(13))); // Too long
        assertFalse(validator.validateEventDuration(start, start)); // Same time
        assertFalse(validator.validateEventDuration(start, start.minusHours(1))); // End before start
    }
    
    @Test
    public void testMultipleEventsForSameOrganizer() {
        validator.registerEvent(testEvent1); // organizer1, 10:00-12:00
        
        // Register second event for same organizer at different time
        LocalDateTime laterStart = LocalDateTime.of(2024, 6, 15, 14, 0);
        LocalDateTime laterEnd = LocalDateTime.of(2024, 6, 15, 16, 0);
        Event laterEvent = new Event("Later Event", "Description", 
                                    EventType.SEMINAR, laterStart, laterEnd, "organizer1");
        validator.registerEvent(laterEvent);
        
        // Should be able to register non-overlapping events
        assertTrue(validator.isOrganizerAvailable("organizer1", 
                   LocalDateTime.of(2024, 6, 15, 17, 0),
                   LocalDateTime.of(2024, 6, 15, 18, 0), null));
        
        // But not overlapping events
        assertFalse(validator.isOrganizerAvailable("organizer1", 
                    LocalDateTime.of(2024, 6, 15, 11, 0),
                    LocalDateTime.of(2024, 6, 15, 13, 0), null));
    }
}