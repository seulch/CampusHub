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
    
    public Registration(String attendeeId, String eventId) {
        // TODO: Generate unique registrationId
        // TODO: Set registration timestamp
        // TODO: Set initial status (PENDING or CONFIRMED based on capacity)
        // TODO: Initialize attendance as false
        // TODO: Set waitlist position if applicable
    }
    
    public void confirmRegistration() {
        // TODO: Update status to CONFIRMED
        // TODO: Send confirmation notification
        // TODO: Add to attendee's personal schedule
        // TODO: Log confirmation event
    }
    
    public void cancelRegistration(String reason) {
        // TODO: Update status to CANCELLED
        // TODO: Set cancellation reason and timestamp
        // TODO: Remove from personal schedule
        // TODO: Promote next person from waitlist
        // TODO: Send cancellation confirmation
    }
    
    public void markAttendance() {
        // TODO: Set attended to true
        // TODO: Record attendance timestamp
        // TODO: Update event attendance statistics
        // TODO: Log attendance marking
    }
    
    public boolean isConfirmed() {
        return status == RegistrationStatus.CONFIRMED;
    }
    
    // TODO: Add getters, setters, validation methods
    // public boolean canBeCancelled()
    // public long getHoursUntilEvent()
    // public boolean isWaitlisted()
}