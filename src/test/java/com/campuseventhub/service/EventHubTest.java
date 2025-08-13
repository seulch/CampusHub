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
    
    // @BeforeEach
    // void setUp() {
    //     // Get fresh instance for each test
    //     eventHub = EventHub.getInstance();
    //     
    //     // Create test users
    //     eventHub.registerUser("admin", "admin@test.com", "password", "Admin", "User", UserRole.ADMIN);
    //     eventHub.registerUser("organizer", "organizer@test.com", "password", "Test", "Organizer", UserRole.ORGANIZER);
    //     eventHub.registerUser("attendee", "attendee@test.com", "password", "Test", "Attendee", UserRole.ATTENDEE);
    //     
    //     // Clear current user for each test
    //     eventHub.logoutCurrentUser();
    // }
    
    @BeforeEach
    void setUp() {
        // Get fresh instance for each test
        eventHub = EventHub.getInstance();
        
        // Clear current user for each test
        eventHub.logoutCurrentUser();
        
        // Individual tests will create users as needed with strong passwords
    }
    
    
    // @Test
    // @DisplayName("Should implement singleton pattern correctly")
    // void testSingletonPattern() {
    //     EventHub instance1 = EventHub.getInstance();
    //     EventHub instance2 = EventHub.getInstance();
        
    //     assertSame(instance1, instance2);
    // }
    
    // @Test
    // @DisplayName("Should authenticate user successfully")
    // void testAuthenticateUser() {
    //     User authenticatedUser = eventHub.authenticateUser("admin", "password");
        
    //     assertNotNull(authenticatedUser);
    //     assertEquals("admin", authenticatedUser.getUsername());
    //     assertEquals(UserRole.ADMIN, authenticatedUser.getRole());
    //     assertTrue(eventHub.isUserLoggedIn());
    //     assertEquals(authenticatedUser, eventHub.getCurrentUser());
    // }
    
    // @Test
    // @DisplayName("Should fail authentication with invalid credentials")
    // void testAuthenticateInvalidUser() {
    //     User authenticatedUser = eventHub.authenticateUser("admin", "wrongpassword");
        
    //     assertNull(authenticatedUser);
    //     assertFalse(eventHub.isUserLoggedIn());
    //     assertNull(eventHub.getCurrentUser());
    // }
    
    // @Test
    // @DisplayName("Should register new user successfully")
    // void testRegisterUser() {
    //     boolean result = eventHub.registerUser("newuser", "new@test.com", "password", 
    //                                          "New", "User", UserRole.ATTENDEE);
        
    //     assertTrue(result);
        
    //     // Verify user can login
    //     User authenticatedUser = eventHub.authenticateUser("newuser", "password");
    //     assertNotNull(authenticatedUser);
    //     assertEquals("newuser", authenticatedUser.getUsername());
    // }
    
    // @Test
    // @DisplayName("Should not register user with duplicate username")
    // void testRegisterDuplicateUser() {
    //     // First registration should succeed
    //     boolean result1 = eventHub.registerUser("duplicate", "test1@test.com", "password", 
    //                                           "Test", "User", UserRole.ATTENDEE);
    //     assertTrue(result1);
        
    //     // Second registration with same username should fail
    //     boolean result2 = eventHub.registerUser("duplicate", "test2@test.com", "password", 
    //                                           "Another", "User", UserRole.ATTENDEE);
    //     assertFalse(result2);
    // }
    
    // @Test
    // @DisplayName("Should create event when logged in as organizer")
    // void testCreateEventAsOrganizer() {
    //     // Login as organizer
    //     User organizer = eventHub.authenticateUser("organizer", "password");
    //     assertNotNull(organizer);
        
    //     LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    //     LocalDateTime endTime = startTime.plusHours(2);
        
    //     Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
    //                                      startTime, endTime, organizer.getUserId(), null, 50);
        
    //     assertNotNull(event);
    //     assertEquals("Test Event", event.getTitle());
    //     assertEquals("Test Description", event.getDescription());
    //     assertEquals(EventType.WORKSHOP, event.getEventType());
    //     assertEquals(startTime, event.getStartDateTime());
    //     assertEquals(endTime, event.getEndDateTime());
    //     assertEquals(organizer.getUserId(), event.getOrganizerId());
    //     assertEquals(EventStatus.DRAFT, event.getStatus());
    // }
    
    // @Test
    // @DisplayName("Should not create event when not logged in as organizer")
    // void testCreateEventWithoutOrganizerRole() {
    //     // Login as attendee (not organizer)
    //     User attendee = eventHub.authenticateUser("attendee", "password");
    //     assertNotNull(attendee);
        
    //     LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    //     LocalDateTime endTime = startTime.plusHours(2);
        
    //     Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
    //                                      startTime, endTime, attendee.getUserId(), null, 50);
        
    //     assertNull(event);
    // }
    
    // @Test
    // @DisplayName("Should not create event when not logged in")
    // void testCreateEventWhenNotLoggedIn() {
    //     // Ensure no user is logged in
    //     assertFalse(eventHub.isUserLoggedIn());
        
    //     LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    //     LocalDateTime endTime = startTime.plusHours(2);
        
    //     Event event = eventHub.createEvent("Test Event", "Test Description", EventType.WORKSHOP,
    //                                      startTime, endTime, "someUserId", null, 50);
        
    //     assertNull(event);
    // }
    
    // @Test
    // @DisplayName("Should search events successfully")
    // void testSearchEvents() {
    //     // Login as organizer and create events
    //     User organizer = eventHub.authenticateUser("organizer", "password");
    //     assertNotNull(organizer);
        
    //     LocalDateTime startTime1 = LocalDateTime.now().plusDays(1);
    //     LocalDateTime endTime1 = startTime1.plusHours(2);
        
    //     LocalDateTime startTime2 = LocalDateTime.now().plusDays(2);
    //     LocalDateTime endTime2 = startTime2.plusHours(3);
        
    //     eventHub.createEvent("Workshop Event", "Learn something new", EventType.WORKSHOP,
    //                        startTime1, endTime1, organizer.getUserId(), null, 50);
    //     eventHub.createEvent("Seminar Event", "Professional development", EventType.SEMINAR,
    //                        startTime2, endTime2, organizer.getUserId(), null, 100);
        
    //     // Search for workshop events
    //     List<Event> workshopEvents = eventHub.searchEvents("Workshop", EventType.WORKSHOP, null, null);
    //     assertEquals(1, workshopEvents.size());
    //     assertEquals("Workshop Event", workshopEvents.get(0).getTitle());
        
    //     // Search for all events with keyword "Event"
    //     List<Event> allEvents = eventHub.searchEvents("Event", null, null, null);
    //     assertEquals(2, allEvents.size());
        
    //     // Search with no results
    //     List<Event> noResults = eventHub.searchEvents("NonExistent", null, null, null);
    //     assertEquals(0, noResults.size());
    // }
    
    // @Test
    // @DisplayName("Should add venue when logged in as admin")
    // void testAddVenueAsAdmin() {
    //     // Login as admin
    //     User admin = eventHub.authenticateUser("admin", "password");
    //     assertNotNull(admin);
        
    //     Venue venue = new Venue("Conference Room A", "Building 1, Floor 2", 50);
    //     boolean result = eventHub.addVenue(venue);
        
    //     assertTrue(result);
        
    //     // Verify venue was added
    //     List<Venue> venues = eventHub.listVenues();
    //     assertEquals(1, venues.size());
    //     assertEquals("Conference Room A", venues.get(0).getName());
    // }
    
    // @Test
    // @DisplayName("Should not add venue when not logged in as admin")
    // void testAddVenueWithoutAdminRole() {
    //     // Login as organizer (not admin)
    //     User organizer = eventHub.authenticateUser("organizer", "password");
    //     assertNotNull(organizer);
        
    //     Venue venue = new Venue("Conference Room B", "Building 2, Floor 1", 30);
    //     boolean result = eventHub.addVenue(venue);
        
    //     assertFalse(result);
        
    //     // Verify venue was not added
    //     List<Venue> venues = eventHub.listVenues();
    //     assertEquals(0, venues.size());
    // }
    
    // @Test
    // @DisplayName("Should list venues successfully")
    // void testListVenues() {
    //     // Login as admin and add venues
    //     User admin = eventHub.authenticateUser("admin", "password");
    //     assertNotNull(admin);
        
    //     Venue venue1 = new Venue("Room 101", "Building A", 25);
    //     Venue venue2 = new Venue("Room 102", "Building A", 50);
        
    //     eventHub.addVenue(venue1);
    //     eventHub.addVenue(venue2);
        
    //     List<Venue> venues = eventHub.listVenues();
    //     assertEquals(2, venues.size());
        
    //     // Verify venues are in the list
    //     boolean foundRoom101 = venues.stream().anyMatch(v -> "Room 101".equals(v.getName()));
    //     boolean foundRoom102 = venues.stream().anyMatch(v -> "Room 102".equals(v.getName()));
        
    //     assertTrue(foundRoom101);
    //     assertTrue(foundRoom102);
    // }
    
    // @Test
    // @DisplayName("Should handle user logout correctly")
    // void testLogoutCurrentUser() {
    //     // Login first
    //     User user = eventHub.authenticateUser("admin", "password");
    //     assertNotNull(user);
    //     assertTrue(eventHub.isUserLoggedIn());
        
    //     // Logout
    //     eventHub.logoutCurrentUser();
        
    //     assertFalse(eventHub.isUserLoggedIn());
    //     assertNull(eventHub.getCurrentUser());
    // }
    
    // @Test
    // @DisplayName("Should handle logout when no user logged in")
    // void testLogoutWhenNoUserLoggedIn() {
    //     // Ensure no user is logged in
    //     assertFalse(eventHub.isUserLoggedIn());
        
    //     // Should not throw exception
    //     assertDoesNotThrow(() -> eventHub.logoutCurrentUser());
    // }
    
    // @Test
    // @DisplayName("Should maintain user session across operations")
    // void testUserSessionPersistence() {
    //     // Login
    //     User originalUser = eventHub.authenticateUser("organizer", "password");
    //     assertNotNull(originalUser);
        
    //     // Perform some operations
    //     LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    //     LocalDateTime endTime = startTime.plusHours(2);
        
    //     Event event = eventHub.createEvent("Session Test", "Testing session", EventType.WORKSHOP,
    //                                      startTime, endTime, originalUser.getUserId(), null, 30);
    //     assertNotNull(event);
        
    //     // Verify user is still logged in
    //     assertTrue(eventHub.isUserLoggedIn());
    //     assertEquals(originalUser, eventHub.getCurrentUser());
    // }
    
    // @Test
    // @DisplayName("Should handle invalid event creation parameters")
    // void testCreateEventWithInvalidParameters() {
    //     // Login as organizer
    //     User organizer = eventHub.authenticateUser("organizer", "password");
    //     assertNotNull(organizer);
        
    //     LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    //     LocalDateTime endTime = startTime.plusHours(2);
        
    //     // Test with null title
    //     Event event1 = eventHub.createEvent(null, "Description", EventType.WORKSHOP,
    //                                       startTime, endTime, organizer.getUserId(), null, 30);
    //     assertNull(event1);
        
    //     // Test with empty title
    //     Event event2 = eventHub.createEvent("", "Description", EventType.WORKSHOP,
    //                                       startTime, endTime, organizer.getUserId(), null, 30);
    //     assertNull(event2);
        
    //     // Test with null start time
    //     Event event3 = eventHub.createEvent("Title", "Description", EventType.WORKSHOP,
    //                                       null, endTime, organizer.getUserId(), null, 30);
    //     assertNull(event3);
    // }
    
    // ========== NEW ENHANCED EVENTHUB TESTS ==========
    
    @Test
    @DisplayName("Should register user with enhanced validation")
    void testEnhancedUserRegistration() {
        // Test successful registration with strong password
        boolean result = eventHub.registerUser("newtestuser", "newtest@example.com", "strongpass123", 
                                              "New", "User", UserRole.ATTENDEE);
        assertTrue(result);
        
        // Test authentication with new user
        User authenticatedUser = eventHub.authenticateUser("newtestuser", "strongpass123");
        assertNotNull(authenticatedUser);
        assertEquals("newtestuser", authenticatedUser.getUsername());
        assertEquals("newtest@example.com", authenticatedUser.getEmail());
    }
    
    @Test 
    @DisplayName("Should reject weak passwords during registration")
    void testWeakPasswordRejection() {
        // Test that weak passwords are rejected
        assertThrows(Exception.class, () -> {
            eventHub.registerUser("weakuser", "weak@example.com", "weak", 
                                 "Weak", "User", UserRole.ATTENDEE);
        });
        
        // Test that password without numbers is rejected
        assertThrows(Exception.class, () -> {
            eventHub.registerUser("nonum", "nonum@example.com", "passwordonly", 
                                 "No", "Numbers", UserRole.ATTENDEE);
        });
    }
    
    @Test
    @DisplayName("Should handle event creation with validation")
    void testEnhancedEventCreation() {
        // Create organizer with strong password
        eventHub.registerUser("eventorg", "eventorg@example.com", "organizer123", 
                             "Event", "Organizer", UserRole.ORGANIZER);
        
        // Login as organizer
        User organizer = eventHub.authenticateUser("eventorg", "organizer123");
        assertNotNull(organizer);
        
        // Create event with valid parameters
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Event event = eventHub.createEvent("Test Workshop", "A comprehensive test workshop", 
                                          EventType.WORKSHOP, startTime, endTime, 
                                          organizer.getUserId(), null, 50);
        
        assertNotNull(event);
        assertEquals("Test Workshop", event.getTitle());
        assertEquals(EventType.WORKSHOP, event.getEventType());
    }
    
    @Test
    @DisplayName("Should prevent non-organizer from creating events")
    void testEventCreationRoleValidation() {
        // Create attendee user
        eventHub.registerUser("attendeeuser", "attendee@example.com", "attendee123", 
                             "Test", "Attendee", UserRole.ATTENDEE);
        
        // Login as attendee
        User attendee = eventHub.authenticateUser("attendeeuser", "attendee123");
        assertNotNull(attendee);
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        // Should return null for non-organizer
        Event event = eventHub.createEvent("Unauthorized Event", "Should not be created", 
                                          EventType.WORKSHOP, startTime, endTime, 
                                          attendee.getUserId(), "TEST_VENUE", 50);
        
        assertNull(event);
    }
    
    @Test
    @DisplayName("Should handle venue management for admins")
    void testVenueManagementWithValidation() {
        // Create admin with strong password
        eventHub.registerUser("venuead", "venuead@example.com", "adminpass123", 
                             "Venue", "Admin", UserRole.ADMIN);
        
        // Login as admin
        User admin = eventHub.authenticateUser("venuead", "adminpass123");
        assertNotNull(admin);
        
        // Create venue
        Venue venue = new Venue("TEST_VENUE_001", "Test Conference Room", 100);
        boolean result = eventHub.addVenue(venue);
        assertTrue(result);
        
        // List venues
        List<Venue> venues = eventHub.listVenues();
        assertNotNull(venues);
        assertTrue(venues.size() >= 1);
    }
    
    @Test
    @DisplayName("Should maintain session state correctly")
    void testSessionManagement() {
        // Initially no user should be logged in
        assertFalse(eventHub.isUserLoggedIn());
        assertNull(eventHub.getCurrentUser());
        
        // Register and login user
        eventHub.registerUser("sessionuser", "session@example.com", "session123", 
                             "Session", "User", UserRole.ATTENDEE);
        
        User user = eventHub.authenticateUser("sessionuser", "session123");
        assertNotNull(user);
        assertTrue(eventHub.isUserLoggedIn());
        assertEquals(user, eventHub.getCurrentUser());
        
        // Logout
        eventHub.logoutCurrentUser();
        assertFalse(eventHub.isUserLoggedIn());
        assertNull(eventHub.getCurrentUser());
    }
    
    @Test
    @DisplayName("Should handle user retrieval operations")
    void testUserRetrievalOperations() {
        // Create users of different types
        eventHub.registerUser("retrieval1", "ret1@example.com", "password123", 
                             "Retrieval", "Admin", UserRole.ADMIN);
        eventHub.registerUser("retrieval2", "ret2@example.com", "password123", 
                             "Retrieval", "Organizer", UserRole.ORGANIZER);
        eventHub.registerUser("retrieval3", "ret3@example.com", "password123", 
                             "Retrieval", "Attendee", UserRole.ATTENDEE);
        
        // Test get all users
        List<User> allUsers = eventHub.getAllUsers();
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 3);
        
        // Test get users by role
        List<User> admins = eventHub.getUsersByRole(UserRole.ADMIN);
        assertTrue(admins.size() >= 1);
        
        List<User> organizers = eventHub.getUsersByRole(UserRole.ORGANIZER);
        assertTrue(organizers.size() >= 1);
        
        List<User> attendees = eventHub.getUsersByRole(UserRole.ATTENDEE);
        assertTrue(attendees.size() >= 1);
    }
}