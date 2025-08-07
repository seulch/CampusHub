// =============================================================================
// EVENT SEARCH CRITERIA MODEL
// =============================================================================

package com.campuseventhub.model.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Criteria for searching and filtering events.
 * 
 * Implementation Details:
 * - Multiple search parameters
 * - Date range filtering
 * - Event type filtering
 * - Venue-based filtering
 * - Keyword search support
 */
public class EventSearchCriteria {
    private String keyword;
    private EventType eventType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String venueId;
    private String organizerId;
    private List<String> tags;
    
    public EventSearchCriteria() {
        this.tags = new ArrayList<>();
    }
    
    // Getters and setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
    
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
} 