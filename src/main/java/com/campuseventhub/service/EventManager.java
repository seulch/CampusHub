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
        loadDataFromPersistence();
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
     * Creates a new event with validation and indexing
     * PARAMS: title, description, type, startTime, endTime, organizerId, venueId
     */
    public Event createEvent(String title, String description, EventType type,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String organizerId, String venueId) {
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
        // Note: For now, we'll track venue ID separately since Event uses Venue object
        // In a complete implementation, we'd create a Venue object here
        
        // Check for conflicts
        List<String> conflicts = scheduleValidator.detectConflicts(event, null);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Event conflicts detected: " + String.join(", ", conflicts));
        }
        
        // Use repository pattern
        create(event);
        
        return event;
    }
    
    /**
     * Updates event information with provided field updates
     * PARAMS: eventId, updates
     */
    public boolean updateEvent(String eventId, Map<String, Object> updates) {
        Event event = events.get(eventId);
        if (event == null) {
            return false;
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
                        event.setMaxCapacity((Integer) value);
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
        return registrationManager.createRegistration(eventId, attendeeId);
    }
    
    public boolean cancelRegistration(String registrationId, String reason) {
        return registrationManager.cancelRegistration(registrationId);
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