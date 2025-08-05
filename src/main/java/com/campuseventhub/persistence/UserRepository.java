// =============================================================================
// PERSISTENCE LAYER
// =============================================================================

package com.campuseventhub.persistence;

import com.campuseventhub.model.user.User;
import java.util.List;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository {
    void create(User user);
    User findById(String userId);
    List<User> findAll();
    void update(User user);
    void deleteById(String userId);
}
