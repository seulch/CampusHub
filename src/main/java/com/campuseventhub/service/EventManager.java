// =============================================================================
// EVENT MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventSearchCriteria;
import com.campuseventhub.model.event.Conflict;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.campuseventhub.model.event.EventStatus;

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
public class EventManager {
    private Map<String, Event> events;
    private Map<String, List<Event>> eventsByOrganizer;
    private Map<EventType, List<Event>> eventsByType;
    private ScheduleValidator scheduleValidator;
    
    /**
     * Initializes thread-safe event storage and indexes
     */
    public EventManager() {
        this.events = new ConcurrentHashMap<>();
        this.eventsByOrganizer = new ConcurrentHashMap<>();
        this.eventsByType = new ConcurrentHashMap<>();
        this.scheduleValidator = new ScheduleValidator();
    }
    
    /**
     * Creates a new event with validation and indexing
     * PARAMS: title, description, type, startTime, endTime, organizerId, venueId
     */
    public Event createEvent(String title, String description, EventType type,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String organizerId, String venueId) {
        if (title == null || title.trim().isEmpty() ||
            description == null || description.trim().isEmpty() ||
            startTime == null || endTime == null ||
            organizerId == null || organizerId.trim().isEmpty()) {
            return null;
        }
        
        if (startTime.isAfter(endTime)) {
            return null; // Invalid time range
        }
        
        Event event = new Event(title, description, type, startTime, endTime, organizerId);
        
        // Add to all indexes
        events.put(event.getEventId(), event);
        
        // Add to organizer index
        eventsByOrganizer.computeIfAbsent(organizerId, k -> new ArrayList<>()).add(event);
        
        // Add to type index
        eventsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(event);
        
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
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        
        // Remove from all indexes
        events.remove(eventId);
        
        // Remove from organizer index
        List<Event> organizerEvents = eventsByOrganizer.get(event.getOrganizerId());
        if (organizerEvents != null) {
            organizerEvents.remove(event);
        }
        
        // Remove from type index
        List<Event> typeEvents = eventsByType.get(event.getEventType());
        if (typeEvents != null) {
            typeEvents.remove(event);
        }
        
        return true;
    }
    
    /**
     * Searches for events based on specified criteria
     * PARAMS: criteria
     */
    public List<Event> searchEvents(EventSearchCriteria criteria) {
        List<Event> results = new ArrayList<>();
        
        for (Event event : events.values()) {
            boolean matches = true;
            
            // Keyword search
            if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String keyword = criteria.getKeyword().toLowerCase();
                if (!event.getTitle().toLowerCase().contains(keyword) &&
                    !event.getDescription().toLowerCase().contains(keyword)) {
                    matches = false;
                }
            }
            
            // Event type filter
            if (criteria.getEventType() != null && event.getEventType() != criteria.getEventType()) {
                matches = false;
            }
            
            // Date range filter
            if (criteria.getStartDate() != null && event.getStartDateTime().isBefore(criteria.getStartDate())) {
                matches = false;
            }
            if (criteria.getEndDate() != null && event.getStartDateTime().isAfter(criteria.getEndDate())) {
                matches = false;
            }
            
            if (matches) {
                results.add(event);
            }
        }
        
        return results;
    }
    
    /**
     * Retrieves events organized by a specific organizer
     * PARAMS: organizerId
     */
    public List<Event> getEventsByOrganizer(String organizerId) {
        return eventsByOrganizer.getOrDefault(organizerId, new ArrayList<>());
    }
    
    /**
     * Retrieves upcoming events (events with start time in the future)
     */
    public List<Event> getUpcomingEvents() {
        List<Event> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Event event : events.values()) {
            if (event.getStartDateTime().isAfter(now)) {
                upcoming.add(event);
            }
        }
        
        return upcoming;
    }
    
    /**
     * Retrieves events filtered by specific event type
     * PARAMS: type
     */
    public List<Event> getEventsByType(EventType type) {
        return eventsByType.getOrDefault(type, new ArrayList<>());
    }
    
    public List<Conflict> validateEventConflicts(Event event) {
        // TODO: Check venue double-booking
        // TODO: Check organizer schedule conflicts
        // TODO: Validate capacity constraints
        // TODO: Check business rules (min/max duration, etc.)
        // TODO: Return list of conflicts found
        return null;
    }
    
    // TODO: Add additional event management methods
    // public boolean approveEvent(String eventId)
    // public Map<String, Integer> getEventStatistics()
}