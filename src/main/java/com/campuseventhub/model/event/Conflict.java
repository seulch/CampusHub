// =============================================================================
// CONFLICT MODEL
// =============================================================================

package com.campuseventhub.model.event;

/**
 * Represents a scheduling conflict between events.
 * 
 * Implementation Details:
 * - Conflict type identification
 * - Conflict severity levels
 * - Conflict resolution suggestions
 * - Affected entities tracking
 */
public class Conflict {
    private String conflictId;
    private ConflictType type;
    private String description;
    private String affectedEventId;
    private String affectedEntityId;
    private ConflictSeverity severity;
    
    public enum ConflictType {
        VENUE_DOUBLE_BOOKING,
        ORGANIZER_SCHEDULE_CONFLICT,
        ATTENDEE_SCHEDULE_CONFLICT,
        CAPACITY_EXCEEDED,
        BUSINESS_RULE_VIOLATION
    }
    
    public enum ConflictSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    public Conflict(String conflictId, ConflictType type, String description) {
        this.conflictId = conflictId;
        this.type = type;
        this.description = description;
        this.severity = ConflictSeverity.MEDIUM;
    }
    
    // Getters and setters
    public String getConflictId() { return conflictId; }
    public void setConflictId(String conflictId) { this.conflictId = conflictId; }
    
    public ConflictType getType() { return type; }
    public void setType(ConflictType type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAffectedEventId() { return affectedEventId; }
    public void setAffectedEventId(String affectedEventId) { this.affectedEventId = affectedEventId; }
    
    public String getAffectedEntityId() { return affectedEntityId; }
    public void setAffectedEntityId(String affectedEntityId) { this.affectedEntityId = affectedEntityId; }
    
    public ConflictSeverity getSeverity() { return severity; }
    public void setSeverity(ConflictSeverity severity) { this.severity = severity; }
} 