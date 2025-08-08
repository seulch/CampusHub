// =============================================================================
// EVENT MODEL
// =============================================================================

package com.campuseventhub.model.event;

import com.campuseventhub.model.venue.Venue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.Serializable;

/**
 * Core event model representing all event information and behavior.
 * 
 * Implementation Details:
 * - Complete event lifecycle management
 * - Registration and waitlist handling
 * - Venue booking integration
 * - QR code generation for check-ins
 * - Comprehensive event validation
 * - State pattern for status transitions
 */
public class Event implements Serializable {
    private String eventId;
    private String title;
    private String description;
    private EventType eventType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Venue venue;
    private int maxCapacity;
    private LocalDateTime registrationDeadline;
    private EventStatus status;
    private String organizerId;
    private List<String> prerequisites;
    private String targetAudience;
    private List<Registration> registrations;
    private Queue<Registration> waitlist;
    private String qrCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    
    /**
     * Creates a new event with basic information
     * PARAMS: title, description, eventType, startDateTime, endDateTime, organizerId
     */
    public Event(String title, String description, EventType eventType,
                LocalDateTime startDateTime, LocalDateTime endDateTime,
                String organizerId) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.organizerId = organizerId;
        this.status = EventStatus.DRAFT;
        this.registrations = new ArrayList<>();
        this.waitlist = new LinkedList<>();
        this.prerequisites = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Adds a new registration for an attendee to this event
     * PARAMS: attendeeId
     */
    public Registration addRegistration(String attendeeId) {
        // Check if registration is open (deadline must be set and not passed)
        if (registrationDeadline == null || !isRegistrationOpen()) {
            return null;
        }
        
        // Check if attendee is already registered
        for (Registration reg : registrations) {
            if (reg.getAttendeeId().equals(attendeeId)) {
                return null; // Already registered
            }
        }
        
        Registration registration = new Registration(attendeeId, this.eventId);
        
        if (registrations.size() < maxCapacity) {
            registration.confirmRegistration();
            registrations.add(registration);
        } else {
            registration.setWaitlistPosition(waitlist.size() + 1);
            waitlist.offer(registration);
        }
        
        return registration;
    }
    
    /**
     * Removes a registration from this event
     * PARAMS: registrationId
     */
    public boolean removeRegistration(String registrationId) {
        for (int i = 0; i < registrations.size(); i++) {
            Registration reg = registrations.get(i);
            if (reg.getRegistrationId().equals(registrationId)) {
                registrations.remove(i);
                promoteFromWaitlist();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates the event status and modification timestamp
     * PARAMS: newStatus
     */
    public void updateStatus(EventStatus newStatus) {
        this.status = newStatus;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Checks if event registration is currently open
     */
    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if registration deadline is set
        if (registrationDeadline == null) {
            return false;
        }
        
        return now.isBefore(registrationDeadline) && 
               status == EventStatus.PUBLISHED;
    }
    
    /**
     * Checks if event has available capacity for new registrations
     */
    public boolean hasCapacity() {
        return registrations.size() < maxCapacity;
    }
    
    /**
     * Calculates the number of available spots for registration
     */
    public int getAvailableSpots() {
        return Math.max(0, maxCapacity - registrations.size());
    }
    
    /**
     * Generates QR code for event check-in
     */
    public String generateQRCode() {
        if (qrCode == null) {
            String eventData = String.format("Event:%s|CheckIn:%s|Time:%s", 
                eventId, eventId, LocalDateTime.now());
            qrCode = eventData; // Simple implementation for project
        }
        return qrCode;
    }
    
    /**
     * Promotes the next person from waitlist to confirmed registration
     */
    private void promoteFromWaitlist() {
        if (!waitlist.isEmpty() && hasCapacity()) {
            Registration promoted = waitlist.poll();
            promoted.confirmRegistration();
            registrations.add(promoted);
            
            // Update waitlist positions
            int position = 1;
            for (Registration reg : waitlist) {
                reg.setWaitlistPosition(position++);
            }
        }
    }
    
    // Getters and setters
    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventType getEventType() { return eventType; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public Venue getVenue() { return venue; }
    public int getMaxCapacity() { return maxCapacity; }
    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public EventStatus getStatus() { return status; }
    public String getOrganizerId() { return organizerId; }
    public List<String> getPrerequisites() { return prerequisites; }
    public String getTargetAudience() { return targetAudience; }
    public List<Registration> getRegistrations() { return registrations; }
    public Queue<Registration> getWaitlist() { return waitlist; }
    public String getQrCode() { return qrCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastModified() { return lastModified; }
    
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }
    public void setVenue(Venue venue) { this.venue = venue; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setRegistrationDeadline(LocalDateTime registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
}