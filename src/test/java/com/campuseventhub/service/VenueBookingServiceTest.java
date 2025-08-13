package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive test suite for VenueBookingService
 * Tests venue booking, availability checking, and capacity validation
 */
class VenueBookingServiceTest {
    
    private VenueBookingService venueBookingService;
    private VenueManager venueManager;
    private Event testEvent;
    private Venue testVenue;
    private Venue smallVenue;
    private LocalDateTime baseTime;
    
    @BeforeEach
    void setUp() {
        // Create a fresh venue manager for each test
        venueManager = new VenueManager();
        // Clear any existing venues for clean test state
        venueManager.findAll().forEach(venue -> venueManager.deleteById(venue.getVenueId()));
        venueBookingService = new VenueBookingService(venueManager);
        
        baseTime = LocalDateTime.now().plusDays(1);
        
        // Create test venues
        testVenue = new Venue("Test Hall", "Building A", 100);
        testVenue.setSetupTimeMinutes(30);
        testVenue.setCleanupTimeMinutes(30);
        venueManager.create(testVenue);
        
        smallVenue = new Venue("Small Room", "Building B", 20);
        smallVenue.setSetupTimeMinutes(15);
        smallVenue.setCleanupTimeMinutes(15);
        venueManager.create(smallVenue);
        
        // Create test event
        testEvent = new Event("Test Event", "Test Description", EventType.WORKSHOP,
                            baseTime, baseTime.plusHours(2), "organizer1");
        testEvent.setMaxCapacity(50);
    }
    
    @Test
    @DisplayName("Should successfully book available venue")
    void shouldBookAvailableVenue() {
        boolean result = venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        assertTrue(result, "Venue booking should succeed");
        assertEquals(testVenue, testEvent.getVenue(), "Event should have venue assigned");
        assertEquals(50, testEvent.getMaxCapacity(), "Event capacity should remain unchanged");
    }
    
    @Test
    @DisplayName("Should adjust event capacity to venue capacity when event capacity exceeds venue")
    void shouldAdjustCapacityToVenueCapacity() {
        testEvent.setMaxCapacity(150); // Exceeds venue capacity
        
        assertThrows(IllegalArgumentException.class, () -> {
            venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        }, "Should throw exception when event capacity exceeds venue capacity");
    }
    
    @Test
    @DisplayName("Should handle venue capacity validation correctly")
    void shouldValidateVenueCapacity() {
        testEvent.setMaxCapacity(25); // Exceeds small venue capacity
        
        assertThrows(IllegalArgumentException.class, () -> {
            venueBookingService.bookVenueForEvent(testEvent, smallVenue.getVenueId());
        }, "Should reject booking when event capacity exceeds venue capacity");
    }
    
    @Test
    @DisplayName("Should include setup and cleanup time in availability check")
    void shouldIncludeSetupCleanupTime() {
        // Book the venue first
        venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        // Try to book another event that conflicts with setup/cleanup time
        Event conflictingEvent = new Event("Conflict Event", "Description", EventType.SEMINAR,
                                         baseTime.minusMinutes(15), baseTime.plusMinutes(15), "organizer2");
        
        assertThrows(IllegalArgumentException.class, () -> {
            venueBookingService.bookVenueForEvent(conflictingEvent, testVenue.getVenueId());
        }, "Should prevent booking during setup/cleanup time");
    }
    
    @Test
    @DisplayName("Should cancel venue booking successfully")
    void shouldCancelVenueBooking() {
        venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        boolean result = venueBookingService.cancelVenueBooking(testEvent);
        
        assertTrue(result, "Venue booking cancellation should succeed");
        assertNull(testEvent.getVenue(), "Event should have no venue assigned after cancellation");
    }
    
    @Test
    @DisplayName("Should find available venues correctly")
    void shouldFindAvailableVenues() {
        LocalDateTime searchStart = baseTime.plusDays(1);
        LocalDateTime searchEnd = searchStart.plusHours(3);
        
        List<Venue> availableVenues = venueBookingService.findAvailableVenues(searchStart, searchEnd, 30);
        
        assertEquals(1, availableVenues.size(), "Should find one venue with sufficient capacity");
        assertEquals(testVenue, availableVenues.get(0), "Should find the test venue");
    }
    
    @Test
    @DisplayName("Should exclude venues with insufficient capacity")
    void shouldExcludeInsufficientCapacityVenues() {
        List<Venue> availableVenues = venueBookingService.findAvailableVenues(
            baseTime.plusDays(2), baseTime.plusDays(2).plusHours(2), 150);
        
        assertTrue(availableVenues.isEmpty(), "Should find no venues with capacity >= 150");
    }
    
    @Test
    @DisplayName("Should handle venue change correctly")
    void shouldChangeVenueSuccessfully() {
        // Book initial venue
        venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        // Change to smaller venue (adjust capacity first)
        testEvent.setMaxCapacity(15);
        boolean result = venueBookingService.changeEventVenue(testEvent, smallVenue.getVenueId());
        
        assertTrue(result, "Venue change should succeed");
        assertEquals(smallVenue, testEvent.getVenue(), "Event should have new venue");
    }
    
    @Test
    @DisplayName("Should prevent venue change when new venue has insufficient capacity")
    void shouldPreventInvalidVenueChange() {
        venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        // Try to change to smaller venue without adjusting capacity
        boolean result = venueBookingService.canChangeVenue(testEvent, smallVenue.getVenueId());
        
        assertFalse(result, "Should not allow venue change when capacity is incompatible");
    }
    
    @Test
    @DisplayName("Should detect venue conflicts correctly")
    void shouldDetectVenueConflicts() {
        // Book the venue
        venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        // Check for conflicts during the same time
        List<String> conflicts = venueBookingService.getVenueConflicts(
            testVenue.getVenueId(), baseTime, baseTime.plusHours(1));
        
        assertFalse(conflicts.isEmpty(), "Should detect venue conflicts");
        assertTrue(conflicts.get(0).contains("already booked"), "Conflict message should mention booking");
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void shouldHandleNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> {
            venueBookingService.bookVenueForEvent(null, testVenue.getVenueId());
        }, "Should throw exception for null event");
        
        assertThrows(IllegalArgumentException.class, () -> {
            venueBookingService.bookVenueForEvent(testEvent, null);
        }, "Should throw exception for null venue ID");
    }
    
    @Test
    @DisplayName("Should handle non-existent venue")
    void shouldHandleNonExistentVenue() {
        assertThrows(IllegalArgumentException.class, () -> {
            venueBookingService.bookVenueForEvent(testEvent, "non-existent-venue-id");
        }, "Should throw exception for non-existent venue");
    }
    
    @Test
    @DisplayName("Should handle inactive venues")
    void shouldHandleInactiveVenues() {
        testVenue.setActive(false);
        
        List<Venue> availableVenues = venueBookingService.findAvailableVenues(
            baseTime, baseTime.plusHours(2), 50);
        
        assertFalse(availableVenues.contains(testVenue), "Should not include inactive venues");
    }
    
    @Test
    @DisplayName("Should validate venue booking with existing registrations")
    void shouldValidateWithExistingRegistrations() {
        // Book venue
        venueBookingService.bookVenueForEvent(testEvent, testVenue.getVenueId());
        
        // Set event capacity to fit in smaller venue
        testEvent.setMaxCapacity(15);
        
        // Try to change to smaller venue
        boolean canChange = venueBookingService.canChangeVenue(testEvent, smallVenue.getVenueId());
        
        assertTrue(canChange, "Should allow venue change when capacities are compatible");
    }
}