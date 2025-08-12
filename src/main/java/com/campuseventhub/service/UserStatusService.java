package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.user.UserStatus;
import com.campuseventhub.persistence.DataManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;

/**
 * Service for managing user status changes and approval workflows.
 * Handles user approval, suspension, and status transitions.
 */
public class UserStatusService {
    
    private final Map<String, User> users;
    
    public UserStatusService(Map<String, User> users) {
        this.users = users;
    }
    
    /**
     * Gets users pending approval (organizers with PENDING status)
     */
    public List<User> getPendingApprovals() {
        List<User> pending = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getStatus() == UserStatus.PENDING_APPROVAL && user.getRole() == UserRole.ORGANIZER) {
                pending.add(user);
            }
        }
        return pending;
    }
    
    /**
     * Approves a user (changes status from PENDING_APPROVAL to ACTIVE)
     */
    public boolean approveUser(String userId) {
        User user = users.get(userId);
        if (user != null && user.getStatus() == UserStatus.PENDING_APPROVAL) {
            user.setStatus(UserStatus.ACTIVE);
            saveUsersToPersistence();
            return true;
        }
        return false;
    }
    
    /**
     * Suspends a user account
     */
    public boolean suspendUser(String userId) {
        User user = users.get(userId);
        if (user != null && user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.SUSPENDED);
            saveUsersToPersistence();
            return true;
        }
        return false;
    }
    
    /**
     * Activates a suspended user account
     */
    public boolean activateUser(String userId) {
        User user = users.get(userId);
        if (user != null && user.getStatus() == UserStatus.SUSPENDED) {
            user.setStatus(UserStatus.ACTIVE);
            saveUsersToPersistence();
            return true;
        }
        return false;
    }
    
    /**
     * Saves user data to persistence
     */
    private void saveUsersToPersistence() {
        try {
            DataManager.saveData("users.ser", users);
            System.out.println("UserStatusService: Successfully saved " + users.size() + " users to persistence");
        } catch (IOException e) {
            System.err.println("UserStatusService: Failed to save users to persistence: " + e.getMessage());
            e.printStackTrace();
        }
    }
}