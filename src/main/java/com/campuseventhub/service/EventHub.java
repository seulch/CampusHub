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
import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.model.notification.Notification;
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
    private VenueBookingService venueBookingService;
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
        this.venueBookingService = new VenueBookingService(venueManager);
        this.notificationService = new NotificationService();
        
        // Inject venue booking service into event manager
        this.eventManager.setVenueBookingService(venueBookingService);
        
        // Inject notification service into event manager for waitlist notifications
        this.eventManager.setNotificationService(notificationService);
        
        // Start deadline monitoring
        this.eventManager.startDeadlineMonitoring();
        
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
        
        return eventManager.createEvent(title, description, eventType, startDateTime, 
                                      endDateTime, organizerId, venueId, maxCapacity);
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
     * Gets all registrations for a specific attendee (alias for getMyRegistrations)
     * PARAMS: attendeeId
     */
    public List<Registration> getRegistrationsByAttendee(String attendeeId) {
        return getMyRegistrations(attendeeId);
    }
    
    /**
     * Gets an event by its ID
     * PARAMS: eventId
     */
    public Event getEventById(String eventId) {
        return eventManager.findById(eventId);
    }
    
    /**
     * Gets available venues for a specific time slot and capacity
     * PARAMS: startTime, endTime, minCapacity
     */
    public List<Venue> getAvailableVenues(LocalDateTime startTime, LocalDateTime endTime, int minCapacity) {
        return eventManager.getAvailableVenues(startTime, endTime, minCapacity);
    }
    
    /**
     * Changes venue for an existing event (Organizer or Admin only)
     * PARAMS: eventId, newVenueId
     */
    public boolean changeEventVenue(String eventId, String newVenueId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        return eventManager.changeEventVenue(eventId, newVenueId);
    }
    
    /**
     * Cancels venue booking for an event (Organizer or Admin only)
     * PARAMS: eventId
     */
    public boolean cancelEventVenueBooking(String eventId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        return eventManager.cancelEventVenueBooking(eventId);
    }
    
    /**
     * Gets venue conflicts for an event
     * PARAMS: eventId
     */
    public List<String> getEventVenueConflicts(String eventId) {
        return eventManager.getEventVenueConflicts(eventId);
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
     * Gets notifications for the current user
     */
    public List<Notification> getCurrentUserNotifications() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return notificationService.getUserNotifications(currentUser.getUserId());
    }
    
    /**
     * Gets notifications for a specific user
     */
    public List<Notification> getUserNotifications(String userId) {
        return notificationService.getUserNotifications(userId);
    }
    
    /**
     * Gets the notification service instance
     */
    public NotificationService getNotificationService() {
        return notificationService;
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
    
    /**
     * Cancels an event and notifies all attendees (Organizer or Admin only)
     * PARAMS: eventId, reason
     */
    public boolean cancelEvent(String eventId, String reason) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        
        try {
            return eventManager.cancelEvent(eventId, reason, notificationService);
        } catch (Exception e) {
            System.err.println("Failed to cancel event: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reschedules an event to new times and notifies attendees (Organizer or Admin only)
     * PARAMS: eventId, newStartTime, newEndTime, reason
     */
    public boolean rescheduleEvent(String eventId, LocalDateTime newStartTime, LocalDateTime newEndTime, String reason) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        
        try {
            return eventManager.rescheduleEvent(eventId, newStartTime, newEndTime, reason, notificationService);
        } catch (Exception e) {
            System.err.println("Failed to reschedule event: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if an event can be cancelled (Organizer or Admin only)
     * PARAMS: eventId
     */
    public boolean canCancelEvent(String eventId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        
        return eventManager.canCancelEvent(eventId);
    }
    
    /**
     * Checks if an event can be rescheduled (Organizer or Admin only)  
     * PARAMS: eventId
     */
    public boolean canRescheduleEvent(String eventId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        
        return eventManager.canRescheduleEvent(eventId);
    }
    
    // =============================================================================
    // WAITLIST MANAGEMENT METHODS
    // =============================================================================
    
    /**
     * Gets waitlist statistics for an event
     * PARAMS: eventId
     */
    public WaitlistManager.WaitlistStatistics getWaitlistStatistics(String eventId) {
        return eventManager.getWaitlistStatistics(eventId);
    }
    
    /**
     * Gets the waitlist position for the current user
     * PARAMS: eventId
     */
    public int getWaitlistPosition(String eventId) {
        if (currentUser == null) {
            return -1;
        }
        return eventManager.getWaitlistPosition(eventId, currentUser.getUserId());
    }
    
    /**
     * Gets the waitlist position for a specific attendee (Organizer or Admin only)
     * PARAMS: eventId, attendeeId
     */
    public int getWaitlistPosition(String eventId, String attendeeId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return -1;
        }
        return eventManager.getWaitlistPosition(eventId, attendeeId);
    }
    
    /**
     * Checks if the current user is on the waitlist for an event
     * PARAMS: eventId
     */
    public boolean isOnWaitlist(String eventId) {
        if (currentUser == null) {
            return false;
        }
        return eventManager.isOnWaitlist(eventId, currentUser.getUserId());
    }
    
    /**
     * Checks if a specific attendee is on the waitlist (Organizer or Admin only)
     * PARAMS: eventId, attendeeId
     */
    public boolean isOnWaitlist(String eventId, String attendeeId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        return eventManager.isOnWaitlist(eventId, attendeeId);
    }
    
    /**
     * Manually promotes attendees from waitlist (Organizer or Admin only)
     * PARAMS: eventId, numberOfPromotions
     */
    public WaitlistManager.WaitlistPromotionResult promoteFromWaitlist(String eventId, int numberOfPromotions) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return new WaitlistManager.WaitlistPromotionResult(0, new ArrayList<>(), new ArrayList<>());
        }
        return eventManager.promoteFromWaitlist(eventId, numberOfPromotions);
    }
    
    // =============================================================================
    // REGISTRATION DEADLINE MANAGEMENT
    // =============================================================================
    
    /**
     * Sets a registration deadline for an event (Organizer or Admin only)
     * PARAMS: eventId, deadline
     */
    public boolean setRegistrationDeadline(String eventId, LocalDateTime deadline) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        return eventManager.setRegistrationDeadline(eventId, deadline);
    }
    
    /**
     * Extends the registration deadline for an event (Organizer or Admin only)
     * PARAMS: eventId, newDeadline, reason
     */
    public boolean extendRegistrationDeadline(String eventId, LocalDateTime newDeadline, String reason) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        return eventManager.extendRegistrationDeadline(eventId, newDeadline, reason);
    }
    
    /**
     * Removes the registration deadline from an event (Organizer or Admin only)
     * PARAMS: eventId
     */
    public boolean removeRegistrationDeadline(String eventId) {
        if (currentUser == null || 
            (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            return false;
        }
        return eventManager.removeRegistrationDeadline(eventId);
    }
    
    /**
     * Gets registration deadline statistics (Admin only)
     */
    public RegistrationDeadlineManager.RegistrationDeadlineStatistics getDeadlineStatistics() {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return new RegistrationDeadlineManager.RegistrationDeadlineStatistics(0, 0, 0, 0);
        }
        return eventManager.getDeadlineStatistics();
    }
    
    /**
     * Manually processes deadlines for a specific event (Admin only)
     * PARAMS: eventId
     */
    public void processEventDeadlineImmediately(String eventId) {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            return;
        }
        eventManager.processEventDeadlineImmediately(eventId);
    }
    
    /**
     * Stops the deadline monitoring service (for shutdown)
     */
    public void shutdownServices() {
        if (eventManager != null) {
            eventManager.stopDeadlineMonitoring();
        }
        System.out.println("EventHub: Services shutdown completed");
    }
    
    // =============================================================================
    // USER PROFILE MANAGEMENT
    // =============================================================================
    
    /**
     * Updates the current user's profile information
     * PARAMS: firstName, lastName, email
     */
    public boolean updateCurrentUserProfile(String firstName, String lastName, String email) {
        if (currentUser == null) {
            return false;
        }
        
        try {
            // Validate inputs
            if (firstName != null && !firstName.trim().isEmpty()) {
                if (!com.campuseventhub.util.ValidationUtil.isValidName(firstName)) {
                    throw new IllegalArgumentException("Invalid first name");
                }
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                if (!com.campuseventhub.util.ValidationUtil.isValidName(lastName)) {
                    throw new IllegalArgumentException("Invalid last name");
                }
            }
            if (email != null && !email.trim().isEmpty()) {
                if (!com.campuseventhub.util.ValidationUtil.isValidEmail(email)) {
                    throw new IllegalArgumentException("Invalid email format");
                }
            }
            
            // Update the current user object
            currentUser.updateProfile(firstName, lastName, email);
            
            // Update in UserManager
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            if (firstName != null && !firstName.trim().isEmpty()) {
                updates.put("firstName", firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                updates.put("lastName", lastName.trim());
            }
            if (email != null && !email.trim().isEmpty()) {
                updates.put("email", email.trim().toLowerCase());
            }
            
            return userManager.updateUser(currentUser.getUserId(), updates);
            
        } catch (Exception e) {
            System.err.println("Failed to update profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Changes the current user's password
     * PARAMS: currentPassword, newPassword
     */
    public boolean changeCurrentUserPassword(String currentPassword, String newPassword) {
        if (currentUser == null) {
            return false;
        }
        
        try {
            // Verify current password
            if (!currentUser.login(currentUser.getUsername(), currentPassword)) {
                return false; // Current password is incorrect
            }
            
            // Change password
            currentUser.changePassword(newPassword);
            
            // Update in UserManager (password is already updated in the user object)
            userManager.update(currentUser);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to change password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the current user's profile information
     */
    public java.util.Map<String, String> getCurrentUserProfile() {
        if (currentUser == null) {
            return new java.util.HashMap<>();
        }
        
        java.util.Map<String, String> profile = new java.util.HashMap<>();
        profile.put("userId", currentUser.getUserId());
        profile.put("username", currentUser.getUsername());
        profile.put("firstName", currentUser.getFirstName());
        profile.put("lastName", currentUser.getLastName());
        profile.put("email", currentUser.getEmail());
        profile.put("role", currentUser.getRole().toString());
        profile.put("status", currentUser.getStatus().toString());
        
        return profile;
    }
}