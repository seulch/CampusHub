// =============================================================================
// EVENT MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventSearchCriteria;
import com.campuseventhub.model.event.Conflict;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.model.event.EventStatus;
import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.persistence.EventRepository;
import com.campuseventhub.persistence.DataManager;
import com.campuseventhub.util.ValidationUtil;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Service for managing events and related operations.
 * 
 * Implementation Details:
 * - Thread-safe event storage and operations
 * - Complex event search and filtering
 * - Schedule conflict detection
 * - Event lifecycle state management
 * - Registration capacity management
 * - Integration with venue and notification services
 */
public class EventManager implements EventRepository {
    private Map<String, Event> events;
    private Map<String, List<Event>> eventsByOrganizer;
    private Map<EventType, List<Event>> eventsByType;
    private ScheduleValidator scheduleValidator;
    private RegistrationManager registrationManager;
    private EventSearchService searchService;
    private VenueBookingService venueBookingService;
    private WaitlistManager waitlistManager;
    private RegistrationDeadlineManager deadlineManager;
    
    /**
     * Initializes thread-safe event storage and indexes
     */
    public EventManager() {
        this.events = new ConcurrentHashMap<>();
        this.eventsByOrganizer = new ConcurrentHashMap<>();
        this.eventsByType = new ConcurrentHashMap<>();
        this.scheduleValidator = new ScheduleValidator();
        this.registrationManager = new RegistrationManager();
        this.searchService = new EventSearchService();
        this.waitlistManager = new WaitlistManager();
        this.deadlineManager = new RegistrationDeadlineManager();
        
        // Set up event lookup for schedule validator
        this.scheduleValidator.setEventLookup(this::findById);
        
        loadDataFromPersistence();
    }
    
    /**
     * Sets the venue booking service (injected from EventHub)
     */
    public void setVenueBookingService(VenueBookingService venueBookingService) {
        this.venueBookingService = venueBookingService;
    }
    
    /**
     * Sets the notification service for waitlist notifications and deadline management
     */
    public void setNotificationService(NotificationService notificationService) {
        this.waitlistManager.setNotificationService(notificationService);
        this.deadlineManager.setNotificationService(notificationService);
        this.deadlineManager.setEventManager(this);
    }
    
    /**
     * Starts the registration deadline monitoring service
     */
    public void startDeadlineMonitoring() {
        if (deadlineManager != null) {
            deadlineManager.startDeadlineMonitoring();
        }
    }
    
    /**
     * Stops the registration deadline monitoring service
     */
    public void stopDeadlineMonitoring() {
        if (deadlineManager != null) {
            deadlineManager.stopDeadlineMonitoring();
        }
    }
    
    /**
     * Creates and persists an event. Implements EventRepository interface.
     */
    @Override
    public void create(Event event) {
        if (event == null || event.getEventId() == null) {
            throw new IllegalArgumentException("Event and event ID cannot be null");
        }
        
        if (events.containsKey(event.getEventId())) {
            throw new IllegalArgumentException("Event with ID already exists: " + event.getEventId());
        }
        
        events.put(event.getEventId(), event);
        eventsByOrganizer.computeIfAbsent(event.getOrganizerId(), k -> new ArrayList<>()).add(event);
        eventsByType.computeIfAbsent(event.getEventType(), k -> new ArrayList<>()).add(event);
        scheduleValidator.registerEvent(event);
        saveEventsToPersistence();
    }
    
    /**
     * Finds event by ID. Implements EventRepository interface.
     */
    @Override
    public Event findById(String eventId) {
        return events.get(eventId);
    }
    
    /**
     * Returns all events. Implements EventRepository interface.
     */
    @Override
    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }
    
    /**
     * Updates existing event. Implements EventRepository interface.
     */
    @Override
    public void update(Event event) {
        if (event == null || event.getEventId() == null) {
            throw new IllegalArgumentException("Event and event ID cannot be null");
        }
        
        Event existingEvent = events.get(event.getEventId());
        if (existingEvent == null) {
            throw new IllegalArgumentException("Event not found: " + event.getEventId());
        }
        
        events.put(event.getEventId(), event);
        event.setLastModified(LocalDateTime.now());
        saveEventsToPersistence();
    }
    
    /**
     * Deletes event by ID. Implements EventRepository interface.
     */
    @Override
    public void deleteById(String eventId) {
        Event event = events.remove(eventId);
        if (event != null) {
            List<Event> organizerEvents = eventsByOrganizer.get(event.getOrganizerId());
            if (organizerEvents != null) {
                organizerEvents.remove(event);
            }
            
            List<Event> typeEvents = eventsByType.get(event.getEventType());
            if (typeEvents != null) {
                typeEvents.remove(event);
            }
            saveEventsToPersistence();
        }
    }

    /**
     * Creates a new event with validation, venue booking, and indexing
     * PARAMS: title, description, type, startTime, endTime, organizerId, venueId, maxCapacity
     */
    public Event createEvent(String title, String description, EventType type,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String organizerId, String venueId, int maxCapacity) {
        // Validate inputs
        if (!ValidationUtil.isValidEventTitle(title)) {
            throw new IllegalArgumentException("Invalid event title");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Event description cannot be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (!scheduleValidator.validateEventDuration(startTime, endTime)) {
            throw new IllegalArgumentException("Invalid event duration");
        }
        if (organizerId == null || organizerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organizer ID cannot be empty");
        }
        
        Event event = new Event(title, description, type, startTime, endTime, organizerId);
        event.setMaxCapacity(maxCapacity);
        
        // Handle venue booking if venue is specified
        if (venueId != null && !venueId.trim().isEmpty() && venueBookingService != null) {
            try {
                boolean booked = venueBookingService.bookVenueForEvent(event, venueId);
                if (!booked) {
                    throw new IllegalArgumentException("Failed to book venue for the event");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Venue booking failed: " + e.getMessage());
            }
        }
        
        // Check for conflicts after venue is assigned
        List<String> conflicts = scheduleValidator.detectConflicts(event, venueId);
        if (!conflicts.isEmpty()) {
            // Cancel venue booking if conflicts found
            if (event.hasVenue() && venueBookingService != null) {
                venueBookingService.cancelVenueBooking(event);
            }
            throw new IllegalArgumentException("Event conflicts detected: " + String.join(", ", conflicts));
        }
        
        // Use repository pattern
        create(event);
        
        return event;
    }
    
    /**
     * Creates a new event with default capacity (for backward compatibility)
     */
    public Event createEvent(String title, String description, EventType type,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String organizerId, String venueId) {
        return createEvent(title, description, type, startTime, endTime, organizerId, venueId, 50);
    }
    
    /**
     * Updates event information with provided field updates
     * PARAMS: eventId, updates
     */
    public boolean updateEvent(String eventId, Map<String, Object> updates) {
        Event event = events.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            
            switch (field) {
                case "title":
                    if (value instanceof String) {
                        event.setTitle((String) value);
                    }
                    break;
                case "description":
                    if (value instanceof String) {
                        event.setDescription((String) value);
                    }
                    break;
                case "maxCapacity":
                    if (value instanceof Integer) {
                        int oldCapacity = event.getMaxCapacity();
                        int newCapacity = (Integer) value;
                        event.setMaxCapacity(newCapacity);
                        
                        // Handle automatic waitlist promotion if capacity increased
                        if (newCapacity > oldCapacity && waitlistManager != null) {
                            WaitlistManager.WaitlistPromotionResult result = 
                                waitlistManager.handleCapacityIncrease(event, oldCapacity, newCapacity);
                            // The waitlist manager handles all notifications
                        }
                    }
                    break;
                case "eventType":
                    if (value instanceof EventType) {
                        event.setEventType((EventType) value);
                    }
                    break;
                case "status":
                    if (value instanceof EventStatus) {
                        event.setStatus((EventStatus) value);
                    }
                    break;
            }
        }
        
        event.setLastModified(LocalDateTime.now());
        return true;
    }
    
    /**
     * Deletes an event from the system
     * PARAMS: eventId
     */
    public boolean deleteEvent(String eventId) {
        Event event = findById(eventId);
        if (event != null) {
            deleteById(eventId);
            return true;
        }
        return false;
    }
    
    /**
     * Searches for events based on specified criteria
     * PARAMS: criteria
     */
    public List<Event> searchEvents(EventSearchCriteria criteria) {
        return searchService.searchEvents(findAll(), criteria);
    }
    
    public List<Event> getEventsByOrganizer(String organizerId) {
        return searchService.getEventsByOrganizer(findAll(), organizerId);
    }
    
    public List<Event> getUpcomingEvents() {
        return searchService.getUpcomingEvents(findAll());
    }
    
    public List<Event> getEventsByType(EventType type) {
        return searchService.getEventsByType(findAll(), type);
    }
    
    public List<Conflict> validateEventConflicts(Event event) {
        List<String> conflictStrings = scheduleValidator.detectConflicts(event, null);
        List<Conflict> conflicts = new ArrayList<>();
        
        for (String conflictString : conflictStrings) {
            // Convert string conflicts to Conflict objects
            Conflict.ConflictType type = conflictString.contains("Venue") ? 
                Conflict.ConflictType.VENUE_DOUBLE_BOOKING : 
                Conflict.ConflictType.ORGANIZER_SCHEDULE_CONFLICT;
            Conflict conflict = new Conflict(event.getEventId(), type, conflictString);
            conflict.setAffectedEventId(event.getEventId());
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
    
    public Registration registerAttendeeForEvent(String attendeeId, String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        
        // Check for attendee scheduling conflicts
        List<Registration> attendeeRegistrations = registrationManager.getAttendeeRegistrations(attendeeId);
        if (scheduleValidator.hasAttendeeConflict(attendeeId, event.getStartDateTime(), event.getEndDateTime(), 
                                                attendeeRegistrations, eventId)) {
            throw new IllegalArgumentException("Schedule conflict: You are already registered for another event during this time period");
        }
        
        return registrationManager.createRegistration(eventId, attendeeId);
    }
    
    public boolean cancelRegistration(String registrationId, String reason) {
        // Find the event for this registration first
        Event event = null;
        for (Event e : events.values()) {
            if (e.getRegistrations() != null) {
                for (Registration reg : e.getRegistrations()) {
                    if (reg.getRegistrationId().equals(registrationId)) {
                        event = e;
                        break;
                    }
                }
            }
            if (event != null) break;
        }
        
        boolean cancelled = registrationManager.cancelRegistration(registrationId);
        
        // If cancellation successful and event found, handle waitlist promotion
        if (cancelled && event != null && waitlistManager != null) {
            WaitlistManager.WaitlistPromotionResult result = 
                waitlistManager.handleRegistrationCancellation(event);
            // The waitlist manager handles all notifications
        }
        
        return cancelled;
    }
    
    public int getCurrentRegistrationCount(String eventId) {
        return (int) registrationManager.getEventRegistrations(eventId).stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.CONFIRMED)
                .count();
    }
    
    public int getWaitlistSize(String eventId) {
        return (int) registrationManager.getEventRegistrations(eventId).stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.WAITLISTED)
                .count();
    }
    
    
    public List<Registration> getEventRegistrations(String eventId) {
        return registrationManager.getEventRegistrations(eventId);
    }
    
    public List<Registration> getAttendeeRegistrations(String attendeeId) {
        return registrationManager.getAttendeeRegistrations(attendeeId);
    }
    
    /**
     * Gets available venues for a specific time slot and capacity
     */
    public List<Venue> getAvailableVenues(LocalDateTime startTime, LocalDateTime endTime, int minCapacity) {
        if (venueBookingService == null) {
            return new ArrayList<>();
        }
        return venueBookingService.findAvailableVenues(startTime, endTime, minCapacity);
    }
    
    /**
     * Changes venue for an existing event
     */
    public boolean changeEventVenue(String eventId, String newVenueId) {
        if (venueBookingService == null) {
            return false;
        }
        
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        
        boolean success = venueBookingService.changeEventVenue(event, newVenueId);
        if (success) {
            event.setLastModified(LocalDateTime.now());
            saveEventsToPersistence();
        }
        
        return success;
    }
    
    /**
     * Cancels venue booking for an event
     */
    public boolean cancelEventVenueBooking(String eventId) {
        if (venueBookingService == null) {
            return false;
        }
        
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        
        boolean success = venueBookingService.cancelVenueBooking(event);
        if (success) {
            event.setLastModified(LocalDateTime.now());
            saveEventsToPersistence();
        }
        
        return success;
    }
    
    /**
     * Gets venue conflicts for an event
     */
    public List<String> getEventVenueConflicts(String eventId) {
        Event event = events.get(eventId);
        if (event == null || !event.hasVenue() || venueBookingService == null) {
            return new ArrayList<>();
        }
        
        return venueBookingService.getVenueConflicts(
            event.getVenueId(), 
            event.getStartDateTime(), 
            event.getEndDateTime()
        );
    }
    
    /**
     * Cancels an event and notifies all registered attendees
     * PARAMS: eventId, reason, notificationService
     */
    public boolean cancelEvent(String eventId, String reason, NotificationService notificationService) {
        Event event = events.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        
        // Check if event can be cancelled
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("Event is already cancelled");
        }
        
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed event");
        }
        
        // Store original status for potential rollback
        EventStatus originalStatus = event.getStatus();
        
        try {
            // Update event status
            event.setStatus(EventStatus.CANCELLED);
            event.setLastModified(LocalDateTime.now());
            
            // Cancel venue booking if exists
            if (event.hasVenue() && venueBookingService != null) {
                venueBookingService.cancelVenueBooking(event);
            }
            
            // Get all registered attendees for notifications
            List<String> attendeeIds = new ArrayList<>();
            if (event.getRegistrations() != null) {
                attendeeIds = event.getRegistrations().stream()
                    .map(registration -> registration.getAttendeeId())
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Send cancellation notifications
            if (notificationService != null && !attendeeIds.isEmpty()) {
                String message = String.format(
                    "Event '%s' scheduled for %s has been cancelled. Reason: %s",
                    event.getTitle(),
                    event.getStartDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    reason != null ? reason : "No reason provided"
                );
                
                notificationService.sendNotification(message, attendeeIds, 
                    com.campuseventhub.model.notification.NotificationType.EVENT_CANCELLATION);
            }
            
            // Handle waitlist notifications - inform waitlisted users that event is cancelled
            if (event.getWaitlist() != null && !event.getWaitlist().isEmpty() && notificationService != null) {
                List<String> waitlistIds = new ArrayList<>();
                event.getWaitlist().forEach(waitlistEntry -> waitlistIds.add(waitlistEntry.toString()));
                
                String waitlistMessage = String.format(
                    "Event '%s' that you were waitlisted for has been cancelled. Reason: %s",
                    event.getTitle(),
                    reason != null ? reason : "No reason provided"
                );
                
                notificationService.sendNotification(waitlistMessage, waitlistIds,
                    com.campuseventhub.model.notification.NotificationType.EVENT_CANCELLATION);
            }
            
            // Persist changes
            update(event);
            
            return true;
            
        } catch (Exception e) {
            // Rollback on failure
            event.setStatus(originalStatus);
            throw new RuntimeException("Failed to cancel event: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reschedules an event to new dates and notifies attendees
     * PARAMS: eventId, newStartTime, newEndTime, reason, notificationService
     */
    public boolean rescheduleEvent(String eventId, LocalDateTime newStartTime, LocalDateTime newEndTime, 
                                 String reason, NotificationService notificationService) {
        Event event = events.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        
        // Validate new times
        if (newStartTime == null || newEndTime == null) {
            throw new IllegalArgumentException("New start and end times cannot be null");
        }
        
        if (!newStartTime.isBefore(newEndTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        if (newStartTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot reschedule to a past date");
        }
        
        // Check if event can be rescheduled
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule a cancelled event");
        }
        
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reschedule a completed event");
        }
        
        // Store original times for potential rollback
        LocalDateTime originalStart = event.getStartDateTime();
        LocalDateTime originalEnd = event.getEndDateTime();
        
        try {
            // Check venue availability for new time if event has venue
            if (event.hasVenue() && venueBookingService != null) {
                // Cancel current booking
                venueBookingService.cancelVenueBooking(event);
                
                // Try to book new time slot
                boolean venueAvailable = venueBookingService.bookVenueForEvent(event, event.getVenueId());
                if (!venueAvailable) {
                    throw new IllegalArgumentException("Venue is not available for the new time slot");
                }
            }
            
            // Update event times
            event.setStartDateTime(newStartTime);
            event.setEndDateTime(newEndTime);
            event.setLastModified(LocalDateTime.now());
            
            // Get all registered attendees for notifications
            List<String> attendeeIds = new ArrayList<>();
            if (event.getRegistrations() != null) {
                attendeeIds = event.getRegistrations().stream()
                    .map(registration -> registration.getAttendeeId())
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Send rescheduling notifications
            if (notificationService != null && !attendeeIds.isEmpty()) {
                String message = String.format(
                    "Event '%s' has been rescheduled. New date/time: %s to %s. Reason: %s",
                    event.getTitle(),
                    newStartTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    newEndTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    reason != null ? reason : "Schedule adjustment"
                );
                
                notificationService.sendNotification(message, attendeeIds,
                    com.campuseventhub.model.notification.NotificationType.EVENT_UPDATE);
            }
            
            // Notify waitlisted users about rescheduling
            if (event.getWaitlist() != null && !event.getWaitlist().isEmpty() && notificationService != null) {
                List<String> waitlistIds = new ArrayList<>();
                event.getWaitlist().forEach(waitlistEntry -> waitlistIds.add(waitlistEntry.toString()));
                
                String waitlistMessage = String.format(
                    "Event '%s' that you are waitlisted for has been rescheduled to %s. Reason: %s",
                    event.getTitle(),
                    newStartTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    reason != null ? reason : "Schedule adjustment"
                );
                
                notificationService.sendNotification(waitlistMessage, waitlistIds,
                    com.campuseventhub.model.notification.NotificationType.EVENT_UPDATE);
            }
            
            // Persist changes
            update(event);
            
            return true;
            
        } catch (Exception e) {
            // Rollback on failure
            event.setStartDateTime(originalStart);
            event.setEndDateTime(originalEnd);
            
            // Try to restore venue booking if it was cancelled
            if (event.hasVenue() && venueBookingService != null) {
                try {
                    venueBookingService.bookVenueForEvent(event, event.getVenueId());
                } catch (Exception venueRestoreException) {
                    // Log venue restore failure but don't throw - focus on original error
                    System.err.println("Failed to restore venue booking during rollback: " + 
                        venueRestoreException.getMessage());
                }
            }
            
            throw new RuntimeException("Failed to reschedule event: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if an event can be cancelled based on business rules
     */
    public boolean canCancelEvent(String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        
        // Cannot cancel already cancelled or completed events
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            return false;
        }
        
        // Can cancel events that are draft, published, or active
        return true;
    }
    
    /**
     * Checks if an event can be rescheduled based on business rules
     */
    public boolean canRescheduleEvent(String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        
        // Cannot reschedule cancelled or completed events
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
            return false;
        }
        
        // Cannot reschedule events that have already started
        if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    // =============================================================================
    // WAITLIST MANAGEMENT METHODS
    // =============================================================================
    
    /**
     * Adds an attendee to the waitlist for an event
     */
    public boolean addToWaitlist(String eventId, String attendeeId) {
        Event event = findById(eventId);
        if (event == null || waitlistManager == null) {
            return false;
        }
        
        Registration registration = new Registration(attendeeId, eventId);
        return waitlistManager.addToWaitlist(event, registration);
    }
    
    /**
     * Removes an attendee from the waitlist
     */
    public boolean removeFromWaitlist(String eventId, String registrationId) {
        Event event = findById(eventId);
        if (event == null || waitlistManager == null) {
            return false;
        }
        
        return waitlistManager.removeFromWaitlist(event, registrationId);
    }
    
    /**
     * Gets waitlist statistics for an event
     */
    public WaitlistManager.WaitlistStatistics getWaitlistStatistics(String eventId) {
        Event event = findById(eventId);
        if (event == null || waitlistManager == null) {
            return new WaitlistManager.WaitlistStatistics(0, 0, 0);
        }
        
        return waitlistManager.getWaitlistStatistics(event);
    }
    
    /**
     * Gets the waitlist position for a specific attendee
     */
    public int getWaitlistPosition(String eventId, String attendeeId) {
        Event event = findById(eventId);
        if (event == null || waitlistManager == null) {
            return -1;
        }
        
        return waitlistManager.getWaitlistPosition(event, attendeeId);
    }
    
    /**
     * Manually promotes attendees from waitlist (for administrative purposes)
     */
    public WaitlistManager.WaitlistPromotionResult promoteFromWaitlist(String eventId, int numberOfPromotions) {
        Event event = findById(eventId);
        if (event == null || waitlistManager == null) {
            return new WaitlistManager.WaitlistPromotionResult(0, new ArrayList<>(), new ArrayList<>());
        }
        
        return waitlistManager.promoteFromWaitlist(event, numberOfPromotions);
    }
    
    /**
     * Checks if an attendee is on the waitlist for an event
     */
    public boolean isOnWaitlist(String eventId, String attendeeId) {
        Event event = findById(eventId);
        if (event == null) {
            return false;
        }
        
        return event.isOnWaitlist(attendeeId);
    }

    // =============================================================================
    // REGISTRATION DEADLINE MANAGEMENT
    // =============================================================================
    
    /**
     * Extends the registration deadline for an event (Organizer or Admin only)
     */
    public boolean extendRegistrationDeadline(String eventId, LocalDateTime newDeadline, String reason) {
        if (deadlineManager == null) {
            return false;
        }
        return deadlineManager.extendRegistrationDeadline(eventId, newDeadline, reason);
    }
    
    /**
     * Manually processes deadlines for a specific event
     */
    public void processEventDeadlineImmediately(String eventId) {
        if (deadlineManager != null) {
            deadlineManager.processEventDeadlineImmediately(eventId);
        }
    }
    
    /**
     * Gets registration deadline statistics
     */
    public RegistrationDeadlineManager.RegistrationDeadlineStatistics getDeadlineStatistics() {
        if (deadlineManager == null) {
            return new RegistrationDeadlineManager.RegistrationDeadlineStatistics(0, 0, 0, 0);
        }
        return deadlineManager.getDeadlineStatistics();
    }
    
    /**
     * Sets a registration deadline for an event
     */
    public boolean setRegistrationDeadline(String eventId, LocalDateTime deadline) {
        Event event = findById(eventId);
        if (event == null || deadline == null) {
            return false;
        }
        
        // Validate deadline is before event start time
        if (!deadline.isBefore(event.getStartDateTime())) {
            return false;
        }
        
        event.setRegistrationDeadline(deadline);
        event.setLastModified(LocalDateTime.now());
        update(event);
        
        return true;
    }
    
    /**
     * Removes the registration deadline from an event (makes registration open indefinitely until event starts)
     */
    public boolean removeRegistrationDeadline(String eventId) {
        Event event = findById(eventId);
        if (event == null) {
            return false;
        }
        
        event.setRegistrationDeadline(null);
        event.setLastModified(LocalDateTime.now());
        update(event);
        
        return true;
    }

    // =============================================================================
    // DATA PERSISTENCE
    // =============================================================================

    /**
     * Loads events and registrations from persistence
     */
    @SuppressWarnings("unchecked")
    private void loadDataFromPersistence() {
        System.out.println("EventManager: Loading data from persistence...");
        try {
            // Load events
            System.out.println("EventManager: Attempting to load events.ser");
            Object eventsData = DataManager.loadData("events.ser");
            if (eventsData instanceof Map) {
                Map<String, Event> loadedEvents = (Map<String, Event>) eventsData;
                for (Event event : loadedEvents.values()) {
                    events.put(event.getEventId(), event);
                    eventsByOrganizer.computeIfAbsent(event.getOrganizerId(), k -> new ArrayList<>()).add(event);
                    eventsByType.computeIfAbsent(event.getEventType(), k -> new ArrayList<>()).add(event);
                    scheduleValidator.registerEvent(event);
                }
                System.out.println("EventManager: Successfully loaded " + loadedEvents.size() + " events from persistence");
            } else {
                System.out.println("EventManager: No events found or invalid data format");
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("EventManager: No existing event data found or failed to load: " + e.getMessage());
        }
        System.out.println("EventManager: Data loading completed. Current state - Events: " + events.size());
    }
    
    /**
     * Saves events to persistence
     */
    private void saveEventsToPersistence() {
        try {
            System.out.println("EventManager: Attempting to save " + events.size() + " events to persistence");
            DataManager.saveData("events.ser", new ConcurrentHashMap<>(events));
            System.out.println("EventManager: Successfully saved events to persistence");
        } catch (IOException e) {
            System.err.println("EventManager: Failed to save events to persistence: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}