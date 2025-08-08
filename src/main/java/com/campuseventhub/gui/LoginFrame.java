// =============================================================================
// GUI FRAMEWORK - LOGIN FRAME
// =============================================================================

package com.campuseventhub.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.gui.admin.AdminDashboard;
import com.campuseventhub.gui.organizer.OrganizerDashboard;
import com.campuseventhub.gui.attendee.AttendeeDashboard;

/**
 * Login window for user authentication.
 * 
 * Implementation Details:
 * - Secure password handling
 * - Input validation and error display
 * - Remember username functionality
 * - Role-based dashboard routing
 * - Professional UI design
 * - Keyboard shortcuts support
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox rememberUsernameCheckbox;
    
    public LoginFrame() {
        setTitle("Campus EventHub - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Center on screen
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        statusLabel = new JLabel("Welcome to Campus EventHub");
        rememberUsernameCheckbox = new JCheckBox("Remember Username");
    }
    
    private void layoutComponents() {
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        add(new JLabel("Campus EventHub"), gbc);
        
        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        add(passwordField, gbc);
        
        // "Remember' checkbox
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        add(rememberUsernameCheckbox, gbc);
        
        // Buttons
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        add(loginButton, gbc);
        
        gbc.gridx = 1;
        add(registerButton, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        add(statusLabel, gbc);
    }
    
    private void setupEventHandlers() {
        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> statusLabel.setText("Register functionality not implemented yet"));
        
        // Handle Enter key in password field
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }
        
        // Simple demo login logic implementation ->> in real app this would call EventHub authentication
        if (username.equals("admin") && password.equals("admin")) {
            statusLabel.setText("Login successful! Opening admin dashboard...");
            openAdminDashboard();
        } else if (username.equals("organizer") && password.equals("organizer")) {
            statusLabel.setText("Login successful! Opening organizer dashboard...");
            openOrganizerDashboard();
        } else if (username.equals("attendee") && password.equals("attendee")) {
            statusLabel.setText("Login successful! Opening attendee dashboard...");
            openAttendeeDashboard();
        } else {
            statusLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
    }
    
    private void openAdminDashboard() {
        try {
            // temporary admin user for testing
            Admin admin = new Admin("admin", "admin@test.com", "admin", "Admin", "User", "SYSTEM_ADMIN");
            AdminDashboard dashboard = new AdminDashboard(admin);
            dashboard.setVisible(true);
            this.dispose(); // Close login window
        } catch (Exception e) {
            statusLabel.setText("Error opening admin dashboard: " + e.getMessage());
        }
    }
    
    private void openOrganizerDashboard() {
        try {
            // temporary organizer user for testing
            Organizer organizer = new Organizer("organizer", "organizer@test.com", "organizer", "Test", "Organizer", "General");
            OrganizerDashboard dashboard = new OrganizerDashboard(organizer);
            dashboard.setVisible(true);
            this.dispose(); // Close login window
        } catch (Exception e) {
            statusLabel.setText("Error opening organizer dashboard: " + e.getMessage());
        }
    }
    
    private void openAttendeeDashboard() {
        try {
            // temporary attendee user for testing
            Attendee attendee = new Attendee("attendee", "attendee@test.com", "attendee", "Test", "Student");
            AttendeeDashboard dashboard = new AttendeeDashboard(attendee);
            dashboard.setVisible(true);
            this.dispose(); // Close login window
        } catch (Exception e) {
            statusLabel.setText("Error opening attendee dashboard: " + e.getMessage());
        }
    }
    
    // TODO: Add utility methods
    // private boolean validateInputs()
    // private void showError(String message)
    // private void clearFields()
    // private void loadRememberedUsername()
}