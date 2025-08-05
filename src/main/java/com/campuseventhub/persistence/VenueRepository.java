// =============================================================================
// PERSISTENCE LAYER
// =============================================================================

package com.campuseventhub.persistence;

import com.campuseventhub.model.venue.Venue;
import java.util.List;

/**
 * Repository interface for managing Venue entities.
 */
public interface VenueRepository {
    void create(Venue venue);
    Venue findById(String venueId);
    List<Venue> findAll();
    void update(Venue venue);
    void deleteById(String venueId);
}
