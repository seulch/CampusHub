// =============================================================================
// VENUE MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.venue.Venue;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing venues and related operations.
 *
 * Implementation Details:
 * - Thread-safe venue storage and indexing
 * - Venue availability and scheduling coordination
 * - Integration with event and notification services
 * - Support for complex venue search and filtering
 */
public class VenueManager {
    private Map<String, Venue> venues;

    /**
     * Initializes thread-safe venue storage
     */
    public VenueManager() {
        this.venues = new ConcurrentHashMap<>();
    }

    /**
     * Adds a new venue to the system
     * PARAMS: venue
     */
    public boolean addVenue(Venue venue) {
        if (venue == null || venue.getVenueId() == null) {
            return false;
        }
        
        venues.put(venue.getVenueId(), venue);
        return true;
    }

    /**
     * Updates venue information with provided field updates
     * PARAMS: venueId, updates
     */
    public boolean updateVenue(String venueId, Map<String, Object> updates) {
        Venue venue = venues.get(venueId);
        if (venue == null) {
            return false;
        }
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            
            switch (field) {
                case "name":
                    if (value instanceof String) {
                        venue.setName((String) value);
                    }
                    break;
                case "capacity":
                    if (value instanceof Integer) {
                        venue.setCapacity((Integer) value);
                    }
                    break;
                case "location":
                    if (value instanceof String) {
                        venue.setLocation((String) value);
                    }
                    break;
            }
        }
        
        return true;
    }

    /**
     * Retrieves all venues in the system
     */
    public List<Venue> listVenues() {
        return new ArrayList<>(venues.values());
    }
    
    /**
     * Retrieves a specific venue by ID
     * PARAMS: venueId
     */
    public Venue getVenueById(String venueId) {
        return venues.get(venueId);
    }
    
    /**
     * Deletes a venue from the system
     * PARAMS: venueId
     */
    public boolean deleteVenue(String venueId) {
        return venues.remove(venueId) != null;
    }
}
