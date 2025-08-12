package com.campuseventhub.gui;

import com.campuseventhub.model.user.User;
import com.campuseventhub.service.EventHub;
import javax.swing.*;

/**
 * Controller for handling login logic and authentication.
 * Separates business logic from UI components.
 */
public class LoginController {
    
    private final EventHub eventHub;
    private final JLabel statusLabel;
    private final JPasswordField passwordField;
    
    public LoginController(JLabel statusLabel, JPasswordField passwordField) {
        this.eventHub = EventHub.getInstance();
        this.statusLabel = statusLabel;
        this.passwordField = passwordField;
    }
    
    /**
     * Performs user authentication with the provided credentials
     */
    public User performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return null;
        }
        
        try {
            User authenticatedUser = eventHub.authenticateUser(username, password);
            
            if (authenticatedUser != null) {
                statusLabel.setText("Login successful! Opening dashboard...");
                return authenticatedUser;
            } else {
                statusLabel.setText("Invalid username or password");
                passwordField.setText("");
                return null;
            }
        } catch (Exception e) {
            statusLabel.setText("Login error: " + e.getMessage());
            passwordField.setText("");
            return null;
        }
    }
    
    /**
     * Validates input parameters
     */
    public boolean validateInputs(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return false;
        }
        if (password == null || password.isEmpty()) {
            statusLabel.setText("Password cannot be empty");
            return false;
        }
        return true;
    }
    
    /**
     * Clears form fields after failed login
     */
    public void clearFieldsAfterFailure() {
        passwordField.setText("");
    }
    
    /**
     * Updates status with error message
     */
    public void showError(String message) {
        statusLabel.setText(message);
    }
}