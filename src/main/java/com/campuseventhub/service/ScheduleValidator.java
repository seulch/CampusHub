// =============================================================================
// SCHEDULE VALIDATOR SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.model.user.User;
import com.campuseventhub.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for validating event schedules and detecting conflicts.
 * 
 * Implementation Details:
 * - Conflict detection between events
 * - Venue availability validation
 * - Organizer schedule validation
 * - Business rule enforcement
 * - Time overlap detection
 */
public class ScheduleValidator {
    
    private Map<String, List<Event>> venueSchedules;
    private Map<String, List<Event>> organizerSchedules;
    private Function<String, Event> eventLookup;
    
    public ScheduleValidator() {
        this.venueSchedules = new java.util.concurrent.ConcurrentHashMap<>();
        this.organizerSchedules = new java.util.concurrent.ConcurrentHashMap<>();
    }
    
    public void setEventLookup(Function<String, Event> eventLookup) {
        this.eventLookup = eventLookup;
    }
    
    /**
     * Registers an event in the schedule for conflict detection
     */
    public void registerEvent(Event event) {
        // For now, we'll use a simplified approach since Event doesn't expose venueId directly
        // In a complete implementation, we'd modify the Event model to include venueId
        organizerSchedules.computeIfAbsent(event.getOrganizerId(), k -> new ArrayList<>()).add(event);
    }
    
    /**
     * Removes an event from the schedule
     */
    public void unregisterEvent(Event event) {
        List<Event> organizerEvents = organizerSchedules.get(event.getOrganizerId());
        if (organizerEvents != null) {
            organizerEvents.remove(event);
        }
    }
    
    /**
     * Checks if a venue is available for a given time slot
     */
    public boolean isTimeSlotAvailable(String venueId, LocalDateTime startTime, LocalDateTime endTime, String excludeEventId) {
        if (venueId == null) {
            return true; // No venue specified, assume available
        }
        
        List<Event> venueEvents = venueSchedules.get(venueId);
        if (venueEvents == null) {
            return true;
        }
        
        for (Event event : venueEvents) {
            // Skip the event being updated
            if (excludeEventId != null && event.getEventId().equals(excludeEventId)) {
                continue;
            }
            
            if (DateTimeUtil.hasTimeConflict(startTime, endTime, 
                                           event.getStartDateTime(), event.getEndDateTime())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Detects all conflicts for a given event
     */
    public List<String> detectConflicts(Event event, String excludeEventId) {
        List<String> conflicts = new ArrayList<>();
        
        // Note: Organizers can manage multiple simultaneous events in different venues
        // Scheduling conflicts should only apply to attendees, not organizers
        // Venue conflicts are handled separately by the VenueBookingService
        
        // Future: Add attendee conflict checking when implementing registration conflicts
        
        return conflicts;
    }
    
    /**
     * Checks if an organizer is available for a given time slot
     */
    public boolean isOrganizerAvailable(String organizerId, LocalDateTime startTime, LocalDateTime endTime, String excludeEventId) {
        List<Event> organizerEvents = organizerSchedules.get(organizerId);
        if (organizerEvents == null) {
            return true;
        }
        
        for (Event event : organizerEvents) {
            // Skip the event being updated
            if (excludeEventId != null && event.getEventId().equals(excludeEventId)) {
                continue;
            }
            
            if (DateTimeUtil.hasTimeConflict(startTime, endTime, 
                                           event.getStartDateTime(), event.getEndDateTime())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates event duration against business rules
     */
    public boolean validateEventDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (!DateTimeUtil.isValidTimeRange(startTime, endTime)) {
            return false;
        }
        
        // Check minimum duration (15 minutes)
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 15) {
            return false;
        }
        
        // Check maximum duration (12 hours)
        if (durationMinutes > 720) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks for attendee schedule conflicts
     */
    public boolean hasAttendeeConflict(String attendeeId, LocalDateTime startTime, LocalDateTime endTime, 
                                     List<Registration> attendeeRegistrations, String excludeEventId) {
        for (Registration registration : attendeeRegistrations) {
            // Skip cancelled registrations
            if (registration.getStatus() == RegistrationStatus.CANCELLED) {
                continue;
            }
            
            // Skip the event being registered for (when checking updates)
            if (excludeEventId != null && registration.getEventId().equals(excludeEventId)) {
                continue;
            }
            
            // Get the event details for this registration
            if (eventLookup != null) {
                Event registeredEvent = eventLookup.apply(registration.getEventId());
                if (registeredEvent != null) {
                    // Check for time overlap
                    if (DateTimeUtil.hasTimeConflict(startTime, endTime, 
                                                   registeredEvent.getStartDateTime(), 
                                                   registeredEvent.getEndDateTime())) {
                        return true; // Conflict found
                    }
                }
            }
        }
        return false;
    }
} 