// =============================================================================
// EVENTHUB SINGLETON SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventSearchCriteria;
import com.campuseventhub.model.report.Report;
import com.campuseventhub.model.venue.Venue;
import java.util.List;
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
     * Initializes all manager services
     */
    private EventHub() {
        this.userManager = new UserManager();
        this.eventManager = new EventManager();
        this.venueManager = new VenueManager();
        this.notificationService = new NotificationService();
        this.isInitialized = true;
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
        
        return eventManager.createEvent(title, description, eventType, startDateTime, 
                                     endDateTime, organizerId, venueId);
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
}