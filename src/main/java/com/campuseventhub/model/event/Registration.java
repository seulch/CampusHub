// =============================================================================
// REGISTRATION MODEL
// =============================================================================

package com.campuseventhub.model.event;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Registration model linking attendees to events.
 * 
 * Implementation Details:
 * - Complete registration lifecycle tracking
 * - Attendance marking and verification
 * - Waitlist position management
 * - Registration status transitions
 * - Payment integration hooks (for future expansion)
 * - Cancellation policy enforcement
 */
public class Registration implements Serializable {
    private String registrationId;
    private String attendeeId;
    private String eventId;
    private LocalDateTime registrationTime;
    private RegistrationStatus status;
    private boolean attended;
    private LocalDateTime attendanceTime;
    private int waitlistPosition;
    private String cancellationReason;
    private LocalDateTime cancellationTime;
    
    /**
     * Creates a new registration for an attendee to an event
     * PARAMS: attendeeId, eventId
     */
    public Registration(String attendeeId, String eventId) {
        this.registrationId = java.util.UUID.randomUUID().toString();
        this.attendeeId = attendeeId;
        this.eventId = eventId;
        this.registrationTime = LocalDateTime.now();
        this.status = RegistrationStatus.PENDING;
        this.attended = false;
        this.waitlistPosition = 0;
    }
    
    /**
     * Confirms the registration status
     */
    public void confirmRegistration() {
        this.status = RegistrationStatus.CONFIRMED;
    }
    
    /**
     * Cancels the registration with optional reason
     * PARAMS: reason
     */
    public void cancelRegistration(String reason) {
        this.status = RegistrationStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancellationTime = LocalDateTime.now();
    }
    
    /**
     * Marks the attendee as present at the event
     */
    public void markAttendance() {
        this.attended = true;
        this.attendanceTime = LocalDateTime.now();
    }
    
    /**
     * Checks if registration is confirmed
     */
    public boolean isConfirmed() {
        return status == RegistrationStatus.CONFIRMED;
    }
    
    /**
     * Checks if registration is on waitlist
     */
    public boolean isWaitlisted() {
        return waitlistPosition > 0;
    }
    
    // Getters and setters
    public String getRegistrationId() { return registrationId; }
    public String getAttendeeId() { return attendeeId; }
    public String getEventId() { return eventId; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }
    public RegistrationStatus getStatus() { return status; }
    public boolean isAttended() { return attended; }
    public LocalDateTime getAttendanceTime() { return attendanceTime; }
    public int getWaitlistPosition() { return waitlistPosition; }
    public String getCancellationReason() { return cancellationReason; }
    public LocalDateTime getCancellationTime() { return cancellationTime; }
    
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public void setAttended(boolean attended) { this.attended = attended; }
    public void setAttendanceTime(LocalDateTime attendanceTime) { this.attendanceTime = attendanceTime; }
    public void setWaitlistPosition(int waitlistPosition) { this.waitlistPosition = waitlistPosition; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public void setCancellationTime(LocalDateTime cancellationTime) { this.cancellationTime = cancellationTime; }
}