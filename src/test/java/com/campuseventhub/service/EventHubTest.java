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
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        // Clear any existing state
        if (eventHub.isUserLoggedIn()) {
            eventHub.logoutCurrentUser();
        }
    }
    
    @Test
    @DisplayName("Should return singleton instance")
    void testGetInstance() {
        EventHub instance1 = EventHub.getInstance();
        EventHub instance2 = EventHub.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser() {
        boolean result = eventHub.registerUser("testuser", "test@test.com", "password123", 
                                             "Test", "User", UserRole.ATTENDEE);
        
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should authenticate user successfully")
    void testAuthenticateUser() {
        // Register user first
        eventHub.registerUser("testuser", "test@test.com", "password123", 
                            "Test", "User", UserRole.ATTENDEE);
        
        User authenticatedUser = eventHub.authenticateUser("testuser", "password123");
        
        assertNotNull(authenticatedUser);
        assertEquals("testuser", authenticatedUser.getUsername());
        assertTrue(eventHub.isUserLoggedIn());
        assertEquals(authenticatedUser, eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should return null for invalid credentials")
    void testAuthenticateUserInvalidCredentials() {
        // Register user first
        eventHub.registerUser("testuser", "test@test.com", "password123", 
                            "Test", "User", UserRole.ATTENDEE);
        
        User authenticatedUser = eventHub.authenticateUser("testuser", "wrongpassword");
        
        assertNull(authenticatedUser);
        assertFalse(eventHub.isUserLoggedIn());
        assertNull(eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should create event when user is organizer")
    void testCreateEventAsOrganizer() {
        // Register and authenticate as organizer
        eventHub.registerUser("organizer", "organizer@test.com", "password123", 
                            "Organizer", "User", UserRole.ORGANIZER);
        eventHub.authenticateUser("organizer", "password123");
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                                         startTime, endTime, "organizer", "venue123", 50);
        
        assertNotNull(event);
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Description", event.getDescription());
        assertEquals(EventType.WORKSHOP, event.getEventType());
    }
    
    @Test
    @DisplayName("Should not create event when user is not organizer")
    void testCreateEventAsNonOrganizer() {
        // Register and authenticate as attendee
        eventHub.registerUser("attendee", "attendee@test.com", "password123", 
                            "Attendee", "User", UserRole.ATTENDEE);
        eventHub.authenticateUser("attendee", "password123");
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                                         startTime, endTime, "attendee", "venue123", 50);
        
        assertNull(event);
    }
    
    @Test
    @DisplayName("Should not create event when not logged in")
    void testCreateEventWhenNotLoggedIn() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                                         startTime, endTime, "organizer", "venue123", 50);
        
        assertNull(event);
    }
    
    @Test
    @DisplayName("Should search events successfully")
    void testSearchEvents() {
        // Register and authenticate as organizer
        eventHub.registerUser("organizer", "organizer@test.com", "password123", 
                            "Organizer", "User", UserRole.ORGANIZER);
        eventHub.authenticateUser("organizer", "password123");
        
        // Create an event
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
                           startTime, endTime, "organizer", "venue123", 50);
        
        // Search for events
        List<Event> events = eventHub.searchEvents("Test", EventType.WORKSHOP, 
                                                  startTime.minusDays(1), endTime.plusDays(1));
        
        assertNotNull(events);
        assertFalse(events.isEmpty());
    }
    
    @Test
    @DisplayName("Should add venue when user is admin")
    void testAddVenueAsAdmin() {
        // Register and authenticate as admin
        eventHub.registerUser("admin", "admin@test.com", "password123", 
                            "Admin", "User", UserRole.ADMIN);
        eventHub.authenticateUser("admin", "password123");
        
        Venue venue = new Venue("Test Venue", "Test Location", 100);
        
        boolean result = eventHub.addVenue(venue);
        
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should not add venue when user is not admin")
    void testAddVenueAsNonAdmin() {
        // Register and authenticate as attendee
        eventHub.registerUser("attendee", "attendee@test.com", "password123", 
                            "Attendee", "User", UserRole.ATTENDEE);
        eventHub.authenticateUser("attendee", "password123");
        
        Venue venue = new Venue("Test Venue", "Test Location", 100);
        
        boolean result = eventHub.addVenue(venue);
        
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should list venues")
    void testListVenues() {
        // Register and authenticate as admin
        eventHub.registerUser("admin", "admin@test.com", "password123", 
                            "Admin", "User", UserRole.ADMIN);
        eventHub.authenticateUser("admin", "password123");
        
        // Add a venue
        Venue venue = new Venue("Test Venue", "Test Location", 100);
        eventHub.addVenue(venue);
        
        List<Venue> venues = eventHub.listVenues();
        
        assertNotNull(venues);
        assertFalse(venues.isEmpty());
    }
    
    @Test
    @DisplayName("Should logout user successfully")
    void testLogoutCurrentUser() {
        // Register and authenticate user
        eventHub.registerUser("testuser", "test@test.com", "password123", 
                            "Test", "User", UserRole.ATTENDEE);
        eventHub.authenticateUser("testuser", "password123");
        
        assertTrue(eventHub.isUserLoggedIn());
        assertNotNull(eventHub.getCurrentUser());
        
        eventHub.logoutCurrentUser();
        
        assertFalse(eventHub.isUserLoggedIn());
        assertNull(eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should handle logout when no user logged in")
    void testLogoutWhenNoUserLoggedIn() {
        assertFalse(eventHub.isUserLoggedIn());
        
        // Should not throw exception
        eventHub.logoutCurrentUser();
        
        assertFalse(eventHub.isUserLoggedIn());
    }
} 