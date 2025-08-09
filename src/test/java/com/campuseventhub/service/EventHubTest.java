package com.campuseventhub.service;

import com.campuseventhub.model.user.*;
import com.campuseventhub.model.event.*;
import com.campuseventhub.model.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

class EventHubTest {
    
    private EventHub eventHub;
    private String adminUserId;
    private String organizerUserId;
    private String attendeeUserId;
    
    @BeforeEach
    void setUp() {
        // Get fresh instance for each test
        eventHub = EventHub.getInstance();
        
        // Create test users
        eventHub.registerUser("admin", "admin@test.com", "password", "Admin", "User", UserRole.ADMIN);
        eventHub.registerUser("organizer", "organizer@test.com", "password", "Test", "Organizer", UserRole.ORGANIZER);
        eventHub.registerUser("attendee", "attendee@test.com", "password", "Test", "Attendee", UserRole.ATTENDEE);
        
        // Clear current user for each test
        eventHub.logoutCurrentUser();
    }
    
    @Test
    @DisplayName("Should implement singleton pattern correctly")
    void testSingletonPattern() {
        EventHub instance1 = EventHub.getInstance();
        EventHub instance2 = EventHub.getInstance();
        
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should authenticate user successfully")
    void testAuthenticateUser() {
        User authenticatedUser = eventHub.authenticateUser("admin", "password");
        
        assertNotNull(authenticatedUser);
        assertEquals("admin", authenticatedUser.getUsername());
        assertEquals(UserRole.ADMIN, authenticatedUser.getRole());
        assertTrue(eventHub.isUserLoggedIn());
        assertEquals(authenticatedUser, eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should fail authentication with invalid credentials")
    void testAuthenticateInvalidUser() {
        User authenticatedUser = eventHub.authenticateUser("admin", "wrongpassword");
        
        assertNull(authenticatedUser);
        assertFalse(eventHub.isUserLoggedIn());
        assertNull(eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterUser() {
        boolean result = eventHub.registerUser("newuser", "new@test.com", "password", 
                                             "New", "User", UserRole.ATTENDEE);
        
        assertTrue(result);
        
        // Verify user can login
        User authenticatedUser = eventHub.authenticateUser("newuser", "password");
        assertNotNull(authenticatedUser);
        assertEquals("newuser", authenticatedUser.getUsername());
    }
    
    @Test
    @DisplayName("Should not register user with duplicate username")
    void testRegisterDuplicateUser() {
        // First registration should succeed
        boolean result1 = eventHub.registerUser("duplicate", "test1@test.com", "password", 
                                              "Test", "User", UserRole.ATTENDEE);
        assertTrue(result1);
        
        // Second registration with same username should fail
        boolean result2 = eventHub.registerUser("duplicate", "test2@test.com", "password", 
                                              "Another", "User", UserRole.ATTENDEE);
        assertFalse(result2);
    }
    
    @Test
    @DisplayName("Should create event when logged in as organizer")
    void testCreateEventAsOrganizer() {
        // Login as organizer
        User organizer = eventHub.authenticateUser("organizer", "password");
        assertNotNull(organizer);
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                                         startTime, endTime, organizer.getUserId(), null, 50);
        
        assertNotNull(event);
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals(EventType.WORKSHOP, event.getEventType());
        assertEquals(startTime, event.getStartDateTime());
        assertEquals(endTime, event.getEndDateTime());
        assertEquals(organizer.getUserId(), event.getOrganizerId());
        assertEquals(EventStatus.DRAFT, event.getStatus());
    }
    
    @Test
    @DisplayName("Should not create event when not logged in as organizer")
    void testCreateEventWithoutOrganizerRole() {
        // Login as attendee (not organizer)
        User attendee = eventHub.authenticateUser("attendee", "password");
        assertNotNull(attendee);
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                                         startTime, endTime, attendee.getUserId(), null, 50);
        
        assertNull(event);
    }
    
    @Test
    @DisplayName("Should not create event when not logged in")
    void testCreateEventWhenNotLoggedIn() {
        // Ensure no user is logged in
        assertFalse(eventHub.isUserLoggedIn());
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                                         startTime, endTime, "someUserId", null, 50);
        
        assertNull(event);
    }
    
    @Test
    @DisplayName("Should search events successfully")
    void testSearchEvents() {
        // Login as organizer and create events
        User organizer = eventHub.authenticateUser("organizer", "password");
        assertNotNull(organizer);
        
        LocalDateTime startTime1 = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime1 = startTime1.plusHours(2);
        
        LocalDateTime startTime2 = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime2 = startTime2.plusHours(3);
        
        eventHub.createEvent("Workshop Event", "Learn something new", EventType.WORKSHOP,
                           startTime1, endTime1, organizer.getUserId(), null, 50);
        eventHub.createEvent("Seminar Event", "Professional development", EventType.SEMINAR,
                           startTime2, endTime2, organizer.getUserId(), null, 100);
        
        // Search for workshop events
        List<Event> workshopEvents = eventHub.searchEvents("Workshop", EventType.WORKSHOP, null, null);
        assertEquals(1, workshopEvents.size());
        assertEquals("Workshop Event", workshopEvents.get(0).getTitle());
        
        // Search for all events with keyword "Event"
        List<Event> allEvents = eventHub.searchEvents("Event", null, null, null);
        assertEquals(2, allEvents.size());
        
        // Search with no results
        List<Event> noResults = eventHub.searchEvents("NonExistent", null, null, null);
        assertEquals(0, noResults.size());
    }
    
    @Test
    @DisplayName("Should add venue when logged in as admin")
    void testAddVenueAsAdmin() {
        // Login as admin
        User admin = eventHub.authenticateUser("admin", "password");
        assertNotNull(admin);
        
        Venue venue = new Venue("Conference Room A", "Building 1, Floor 2", 50);
        boolean result = eventHub.addVenue(venue);
        
        assertTrue(result);
        
        // Verify venue was added
        List<Venue> venues = eventHub.listVenues();
        assertEquals(1, venues.size());
        assertEquals("Conference Room A", venues.get(0).getName());
    }
    
    @Test
    @DisplayName("Should not add venue when not logged in as admin")
    void testAddVenueWithoutAdminRole() {
        // Login as organizer (not admin)
        User organizer = eventHub.authenticateUser("organizer", "password");
        assertNotNull(organizer);
        
        Venue venue = new Venue("Conference Room B", "Building 2, Floor 1", 30);
        boolean result = eventHub.addVenue(venue);
        
        assertFalse(result);
        
        // Verify venue was not added
        List<Venue> venues = eventHub.listVenues();
        assertEquals(0, venues.size());
    }
    
    @Test
    @DisplayName("Should list venues successfully")
    void testListVenues() {
        // Login as admin and add venues
        User admin = eventHub.authenticateUser("admin", "password");
        assertNotNull(admin);
        
        Venue venue1 = new Venue("Room 101", "Building A", 25);
        Venue venue2 = new Venue("Room 102", "Building A", 50);
        
        eventHub.addVenue(venue1);
        eventHub.addVenue(venue2);
        
        List<Venue> venues = eventHub.listVenues();
        assertEquals(2, venues.size());
        
        // Verify venues are in the list
        boolean foundRoom101 = venues.stream().anyMatch(v -> "Room 101".equals(v.getName()));
        boolean foundRoom102 = venues.stream().anyMatch(v -> "Room 102".equals(v.getName()));
        
        assertTrue(foundRoom101);
        assertTrue(foundRoom102);
    }
    
    @Test
    @DisplayName("Should handle user logout correctly")
    void testLogoutCurrentUser() {
        // Login first
        User user = eventHub.authenticateUser("admin", "password");
        assertNotNull(user);
        assertTrue(eventHub.isUserLoggedIn());
        
        // Logout
        eventHub.logoutCurrentUser();
        
        assertFalse(eventHub.isUserLoggedIn());
        assertNull(eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should handle logout when no user logged in")
    void testLogoutWhenNoUserLoggedIn() {
        // Ensure no user is logged in
        assertFalse(eventHub.isUserLoggedIn());
        
        // Should not throw exception
        assertDoesNotThrow(() -> eventHub.logoutCurrentUser());
    }
    
    @Test
    @DisplayName("Should maintain user session across operations")
    void testUserSessionPersistence() {
        // Login
        User originalUser = eventHub.authenticateUser("organizer", "password");
        assertNotNull(originalUser);
        
        // Perform some operations
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Session Test", "Testing session", EventType.WORKSHOP,
                                         startTime, endTime, originalUser.getUserId(), null, 30);
        assertNotNull(event);
        
        // Verify user is still logged in
        assertTrue(eventHub.isUserLoggedIn());
        assertEquals(originalUser, eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should handle invalid event creation parameters")
    void testCreateEventWithInvalidParameters() {
        // Login as organizer
        User organizer = eventHub.authenticateUser("organizer", "password");
        assertNotNull(organizer);
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Test with null title
        Event event1 = eventHub.createEvent(null, "Description", EventType.WORKSHOP,
                                          startTime, endTime, organizer.getUserId(), null, 30);
        assertNull(event1);
        
        // Test with empty title
        Event event2 = eventHub.createEvent("", "Description", EventType.WORKSHOP,
                                          startTime, endTime, organizer.getUserId(), null, 30);
        assertNull(event2);
        
        // Test with null start time
        Event event3 = eventHub.createEvent("Title", "Description", EventType.WORKSHOP,
                                          null, endTime, organizer.getUserId(), null, 30);
        assertNull(event3);
    }
}