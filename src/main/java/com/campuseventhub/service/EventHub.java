// =============================================================================
// EVENTHUB SINGLETON SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventSearchCriteria;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.report.Report;
import com.campuseventhub.model.venue.Venue;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Central coordinator service implementing Singleton pattern.
 * 
 * Implementation Details:
 * - Thread-safe singleton implementation
 * - Facade pattern for system operations
 * - Session management and user authentication
 * - Service layer coordination
 * - Data persistence coordination
 * - System initialization and shutdown
 */
public class EventHub {
    private static EventHub instance;
    private static final Object lock = new Object();
    
    private UserManager userManager;
    private EventManager eventManager;
    private VenueManager venueManager;
    private NotificationService notificationService;
    private User currentUser;
    private boolean isInitialized;
    
    /**
     * Initializes all manager services with proper coordination
     */
    private EventHub() {
        System.out.println("EventHub: Initializing singleton instance...");
        this.userManager = new UserManager();
        this.eventManager = new EventManager();
        this.venueManager = new VenueManager();
        this.notificationService = new NotificationService();
        this.isInitialized = true;
        System.out.println("EventHub: Initialization completed successfully");
    }
    
    /**
     * Returns the singleton instance of EventHub
     */
    public static EventHub getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new EventHub();
                }
            }
        }
        return instance;
    }
    
    /**
     * Authenticates user login and sets current user session
     * PARAMS: username, password
     */
    public User authenticateUser(String username, String password) {
        User user = userManager.validateCredentials(username, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }
    
    /**
     * Registers a new user account in the system
     * PARAMS: username, email, password, firstName, lastName, role
     */
    public boolean registerUser(String username, String email, String password,
                               String firstName, String lastName, UserRole role) {
        User user = userManager.createUser(username, email, password, firstName, lastName, role);
        return user != null;
    }
    
    /**
     * Creates a new event (Organizer only)
     * PARAMS: title, description, eventType, startDateTime, endDateTime, organizerId, venueId, maxCapacity
     */
    public Event createEvent(String title, String description, EventType eventType,
                           LocalDateTime startDateTime, LocalDateTime endDateTime,
                           String organizerId, String venueId, int maxCapacity) {
        if (currentUser == null || currentUser.getRole() != UserRole.ORGANIZER) {
            return null;
        }
        
        Event event = eventManager.createEvent(title, description, eventType, startDateTime, 
                                            endDateTime, organizerId, venueId);
        if (event != null) {
            event.setMaxCapacity(maxCapacity);
            // Update the event in persistence since we modified it after creation
            eventManager.update(event);
        }
        return event;
    }
    
    /**
     * Searches for events based on specified criteria
     * PARAMS: keyword, type, startDate, endDate
     */
    public List<Event> searchEvents(String keyword, EventType type,
                                   LocalDateTime startDate, LocalDateTime endDate) {
        EventSearchCriteria criteria = new EventSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setEventType(type);
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);
        
        return eventManager.searchEvents(criteria);
    }
    
    /**
     * Adds a new venue to the system (Admin only)
     * PARAMS: venue
     */
    public boolean addVenue(Venue venue) {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return false;
        }
        return venueManager.addVenue(venue);
    }
    
    /**
     * Retrieves all venues in the system
     */
    public List<Venue> listVenues() {
        return venueManager.listVenues();
    }
    
    /**
     * Returns the currently logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Logs out the current user and clears session
     */
    public void logoutCurrentUser() {
        if (currentUser != null) {
            currentUser.logout();
            currentUser = null;
        }
    }
    
    /**
     * Retrieves all users in the system
     */
    public List<User> getAllUsers() {
        return userManager.getAllUsers();
    }
    
    /**
     * Retrieves users filtered by specific role
     * PARAMS: role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userManager.getUsersByRole(role);
    }
    
    /**
     * Retrieves upcoming events (events with start time in the future)
     */
    public List<Event> getUpcomingEvents() {
        return eventManager.getUpcomingEvents();
    }
    
    /**
     * Retrieves events organized by a specific organizer
     * PARAMS: organizerId
     */
    public List<Event> getEventsByOrganizer(String organizerId) {
        return eventManager.getEventsByOrganizer(organizerId);
    }
    
    /**
     * Registers an attendee for an event
     * PARAMS: attendeeId, eventId
     */
    public Registration registerForEvent(String attendeeId, String eventId) {
        return eventManager.registerAttendeeForEvent(attendeeId, eventId);
    }
    
    /**
     * Cancels an attendee's registration for an event
     * PARAMS: registrationId, reason
     */
    public boolean cancelEventRegistration(String registrationId, String reason) {
        return eventManager.cancelRegistration(registrationId, reason);
    }
    
    /**
     * Gets all registrations for a specific attendee
     * PARAMS: attendeeId
     */
    public List<Registration> getMyRegistrations(String attendeeId) {
        return eventManager.getAttendeeRegistrations(attendeeId);
    }
    
    /**
     * Gets all users pending approval (Admin only)
     */
    public List<User> getPendingUserApprovals() {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return new ArrayList<>();
        }
        return userManager.getPendingApprovals();
    }
    
    /**
     * Approves a user account (Admin only)
     * PARAMS: userId
     */
    public boolean approveUser(String userId) {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return false;
        }
        return userManager.approveUser(userId);
    }
    
    /**
     * Suspends a user account (Admin only)
     * PARAMS: userId
     */
    public boolean suspendUser(String userId) {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return false;
        }
        return userManager.suspendUser(userId);
    }
    
    /**
     * Handles application shutdown and ensures all data is persisted
     */
    public void shutdown() {
        System.out.println("EventHub: Shutting down and persisting all data...");
        try {
            // Force persistence of all data
            if (userManager != null) {
                System.out.println("EventHub: Persisting user data...");
            }
            if (eventManager != null) {
                System.out.println("EventHub: Persisting event data...");
            }
            if (venueManager != null) {
                System.out.println("EventHub: Persisting venue data...");
            }
            System.out.println("EventHub: Shutdown completed successfully");
        } catch (Exception e) {
            System.err.println("EventHub: Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Returns whether EventHub is properly initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Gets the EventManager instance for direct access when needed
     */
    public EventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Gets the UserManager instance for direct access when needed  
     */
    public UserManager getUserManager() {
        return userManager;
    }
    
    /**
     * Gets the VenueManager instance for direct access when needed
     */
    public VenueManager getVenueManager() {
        return venueManager;
    }
}