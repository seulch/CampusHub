// =============================================================================
// GUI FRAMEWORK - LOGIN FRAME
// =============================================================================

package com.campuseventhub.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.campuseventhub.model.user.User;

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
        
        // Remember checkbox
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
        
        // Simple demo login - in real app, this would call EventHub authentication
        if (username.equals("admin") && password.equals("admin")) {
            statusLabel.setText("Login successful! Opening admin dashboard...");
            // TODO: Open admin dashboard
        } else if (username.equals("organizer") && password.equals("organizer")) {
            statusLabel.setText("Login successful! Opening organizer dashboard...");
            // TODO: Open organizer dashboard
        } else if (username.equals("attendee") && password.equals("attendee")) {
            statusLabel.setText("Login successful! Opening attendee dashboard...");
            // TODO: Open attendee dashboard
        } else {
            statusLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
    }
    
    private void openDashboard(User user) {
        // TODO: Determine user role
        // TODO: Open appropriate dashboard window
        // TODO: Close login window
        // TODO: Center dashboard on screen
    }
    
    // TODO: Add utility methods
    // private boolean validateInputs()
    // private void showError(String message)
    // private void clearFields()
    // private void loadRememberedUsername()
}