// =============================================================================
// PERSISTENCE LAYER
// =============================================================================

package com.campuseventhub.persistence;

import com.campuseventhub.model.event.Event;
import java.util.List;

/**
 * Repository interface for managing Event entities.
 */
public interface EventRepository {
    void create(Event event);
    Event findById(String eventId);
    List<Event> findAll();
    void update(Event event);
    void deleteById(String eventId);
}
