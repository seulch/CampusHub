package com.campuseventhub.gui;

import javax.swing.*;
import java.awt.*;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.service.EventHub;

/**
 * Registration window for new user account creation.
 */
public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<UserRole> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    public RegistrationFrame() {
        setTitle("Campus EventHub - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        roleComboBox = new JComboBox<>(new UserRole[]{UserRole.ATTENDEE, UserRole.ORGANIZER});
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
        statusLabel = new JLabel("Please fill in all fields");
        statusLabel.setForeground(Color.BLUE);
    }
    
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(new JLabel("Create New Account"), gbc);
        
        // Username
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(usernameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(emailField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(confirmPasswordField, gbc);
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(lastNameField, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 7; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(roleComboBox, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 8; gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);
        gbc.gridx = 1;
        add(cancelButton, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(statusLabel, gbc);
        
        // Password requirements
        gbc.gridy = 10;
        JLabel passwordHelp = new JLabel("<html><small>Password must be 8+ characters with letters and numbers</small></html>");
        passwordHelp.setForeground(Color.GRAY);
        add(passwordHelp, gbc);
    }
    
    private void setupEventHandlers() {
        registerButton.addActionListener(e -> performRegistration());
        cancelButton.addActionListener(e -> this.dispose());
        
        // Handle Enter key
        confirmPasswordField.addActionListener(e -> performRegistration());
    }
    
    private void performRegistration() {
        // Get input values
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        UserRole role = (UserRole) roleComboBox.getSelectedItem();
        
        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || 
            firstName.isEmpty() || lastName.isEmpty()) {
            showError("All fields are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        // Attempt registration
        try {
            EventHub eventHub = EventHub.getInstance();
            boolean success = eventHub.registerUser(username, email, password, firstName, lastName, role);
            
            if (success) {
                showSuccess("Registration successful! You can now login.");
                // Clear fields
                clearFields();
                // Close registration window after a delay
                Timer timer = new Timer(2000, e -> this.dispose());
                timer.setRepeats(false);
                timer.start();
            } else {
                showError("Registration failed. Username or email may already exist.");
            }
        } catch (IllegalArgumentException e) {
            showError("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.GREEN);
    }
    
    private void clearFields() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        roleComboBox.setSelectedIndex(0);
        statusLabel.setText("Please fill in all fields");
        statusLabel.setForeground(Color.BLUE);
    }
}