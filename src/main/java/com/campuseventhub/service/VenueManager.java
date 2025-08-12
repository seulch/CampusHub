// =============================================================================
// VENUE MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.persistence.VenueRepository;
import com.campuseventhub.persistence.DataManager;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

/**
 * Service for managing venues and related operations.
 *
 * Implementation Details:
 * - Thread-safe venue storage and indexing
 * - Venue availability and scheduling coordination
 * - Integration with event and notification services
 * - Support for complex venue search and filtering
 */
public class VenueManager implements VenueRepository {
    private Map<String, Venue> venues;

    /**
     * Initializes thread-safe venue storage
     */
    public VenueManager() {
        this.venues = new ConcurrentHashMap<>();
        loadVenuesFromPersistence();
    }

    /**
     * Creates and persists a venue. Implements VenueRepository interface.
     */
    @Override
    public void create(Venue venue) {
        if (venue == null || venue.getVenueId() == null) {
            throw new IllegalArgumentException("Venue and venue ID cannot be null");
        }
        
        if (venues.containsKey(venue.getVenueId())) {
            throw new IllegalArgumentException("Venue with ID already exists: " + venue.getVenueId());
        }
        
        venues.put(venue.getVenueId(), venue);
        saveVenuesToPersistence();
    }
    
    /**
     * Finds venue by ID. Implements VenueRepository interface.
     */
    @Override
    public Venue findById(String venueId) {
        return venues.get(venueId);
    }
    
    /**
     * Returns all venues. Implements VenueRepository interface.
     */
    @Override
    public List<Venue> findAll() {
        return new ArrayList<>(venues.values());
    }
    
    /**
     * Updates existing venue. Implements VenueRepository interface.
     */
    @Override
    public void update(Venue venue) {
        if (venue == null || venue.getVenueId() == null) {
            throw new IllegalArgumentException("Venue and venue ID cannot be null");
        }
        
        if (!venues.containsKey(venue.getVenueId())) {
            throw new IllegalArgumentException("Venue not found: " + venue.getVenueId());
        }
        
        venues.put(venue.getVenueId(), venue);
        saveVenuesToPersistence();
    }
    
    /**
     * Deletes venue by ID. Implements VenueRepository interface.
     */
    @Override
    public void deleteById(String venueId) {
        if (venues.remove(venueId) != null) {
            saveVenuesToPersistence();
        }
    }

    /**
     * Adds a new venue to the system
     * PARAMS: venue
     */
    public boolean addVenue(Venue venue) {
        try {
            // Check for duplicate venue names to prevent duplicates
            for (Venue existingVenue : venues.values()) {
                if (existingVenue.getName().equals(venue.getName()) && 
                    existingVenue.getLocation().equals(venue.getLocation())) {
                    System.out.println("VenueManager: Venue already exists with name '" + venue.getName() + "' at location '" + venue.getLocation() + "'");
                    return false;
                }
            }
            
            create(venue);
            return true;
        } catch (Exception e) {
            System.err.println("VenueManager: Failed to add venue: " + e.getMessage());
            return false;
        }
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
        
        saveVenuesToPersistence();
        return true;
    }

    /**
     * Retrieves all venues in the system
     */
    public List<Venue> listVenues() {
        return findAll();
    }
    
    /**
     * Retrieves a specific venue by ID
     * PARAMS: venueId
     */
    public Venue getVenueById(String venueId) {
        return findById(venueId);
    }
    
    /**
     * Deletes a venue from the system
     * PARAMS: venueId
     */
    public boolean deleteVenue(String venueId) {
        Venue venue = findById(venueId);
        if (venue != null) {
            deleteById(venueId);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if venue exists by name and location
     * PARAMS: name, location
     */
    public boolean venueExists(String name, String location) {
        for (Venue venue : venues.values()) {
            if (venue.getName().equals(name) && venue.getLocation().equals(location)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Loads venues from persistence
     */
    @SuppressWarnings("unchecked")
    private void loadVenuesFromPersistence() {
        try {
            Object venuesData = DataManager.loadData("venues.ser");
            if (venuesData instanceof Map) {
                Map<String, Venue> loadedVenues = (Map<String, Venue>) venuesData;
                venues.putAll(loadedVenues);
                System.out.println("Loaded " + loadedVenues.size() + " venues from persistence");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing venue data found or failed to load: " + e.getMessage());
        }
    }
    
    /**
     * Saves venues to persistence
     */
    private void saveVenuesToPersistence() {
        try {
            DataManager.saveData("venues.ser", venues);
            System.out.println("Saved " + venues.size() + " venues to persistence");
        } catch (IOException e) {
            System.err.println("Failed to save venues to persistence: " + e.getMessage());
        }
    }
}
