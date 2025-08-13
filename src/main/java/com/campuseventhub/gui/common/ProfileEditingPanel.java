// =============================================================================
// PROFILE EDITING PANEL
// =============================================================================

package com.campuseventhub.gui.common;

import com.campuseventhub.service.EventHub;
import com.campuseventhub.util.ValidationUtil;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Reusable profile editing panel for all user types.
 * 
 * Implementation Details:
 * - Form validation with real-time feedback
 * - Separate sections for profile info and password change
 * - Consistent styling with other panels
 * - Error handling and success notifications
 * - Password confirmation and current password verification
 */
public class ProfileEditingPanel extends JPanel {
    private EventHub eventHub;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton updateProfileButton;
    private JButton changePasswordButton;
    private JLabel statusLabel;
    private JLabel passwordStatusLabel;
    
    public ProfileEditingPanel(EventHub eventHub) {
        this.eventHub = eventHub;
        
        initializeComponents();
        setupEventHandlers();
        loadCurrentProfile();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Main content panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Profile Information Section
        JPanel profilePanel = createProfileInfoPanel();
        mainPanel.add(profilePanel);
        
        // Add spacing
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Password Change Section
        JPanel passwordPanel = createPasswordChangePanel();
        mainPanel.add(passwordPanel);
        
        // Add spacing
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Status labels
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel);
        
        add(mainPanel, BorderLayout.NORTH);
    }
    
    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Profile Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Username (read-only)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setEnabled(false); // Username cannot be changed
        panel.add(usernameField, gbc);
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        firstNameField = new JTextField(20);
        panel.add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Update Profile Button
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 5, 5, 5);
        updateProfileButton = new JButton("Update Profile");
        updateProfileButton.setPreferredSize(new Dimension(150, 30));
        panel.add(updateProfileButton, gbc);
        
        return panel;
    }
    
    private JPanel createPasswordChangePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Change Password"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Current Password
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Current Password:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        currentPasswordField = new JPasswordField(20);
        panel.add(currentPasswordField, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("New Password:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);
        
        // Password requirements label
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 5, 5, 5);
        JLabel requirementsLabel = new JLabel("<html><small><i>Password must be at least 8 characters with letters and numbers</i></small></html>");
        requirementsLabel.setForeground(Color.GRAY);
        panel.add(requirementsLabel, gbc);
        
        // Change Password Button
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 5, 5, 5);
        changePasswordButton = new JButton("Change Password");
        changePasswordButton.setPreferredSize(new Dimension(150, 30));
        panel.add(changePasswordButton, gbc);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel);
        
        passwordStatusLabel = new JLabel(" ");
        passwordStatusLabel.setForeground(Color.BLUE);
        panel.add(passwordStatusLabel);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        updateProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProfile();
            }
        });
        
        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
    }
    
    private void loadCurrentProfile() {
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        
        if (!profile.isEmpty()) {
            usernameField.setText(profile.get("username"));
            firstNameField.setText(profile.get("firstName"));
            lastNameField.setText(profile.get("lastName"));
            emailField.setText(profile.get("email"));
        }
    }
    
    private void updateProfile() {
        try {
            // Clear previous status
            statusLabel.setText(" ");
            
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            
            // Validate inputs
            if (firstName.isEmpty()) {
                showError("First name cannot be empty");
                return;
            }
            if (lastName.isEmpty()) {
                showError("Last name cannot be empty");
                return;
            }
            if (email.isEmpty()) {
                showError("Email cannot be empty");
                return;
            }
            
            // Additional validation
            if (!ValidationUtil.isValidName(firstName)) {
                showError("Invalid first name format");
                return;
            }
            if (!ValidationUtil.isValidName(lastName)) {
                showError("Invalid last name format");
                return;
            }
            if (!ValidationUtil.isValidEmail(email)) {
                showError("Invalid email format");
                return;
            }
            
            // Update profile
            boolean success = eventHub.updateCurrentUserProfile(firstName, lastName, email);
            
            if (success) {
                statusLabel.setText("Profile updated successfully!");
                statusLabel.setForeground(Color.GREEN);
                
                // Clear the status after 3 seconds
                Timer timer = new Timer(3000, e -> statusLabel.setText(" "));
                timer.setRepeats(false);
                timer.start();
            } else {
                showError("Failed to update profile. Please try again.");
            }
            
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }
    
    private void changePassword() {
        try {
            // Clear previous status
            passwordStatusLabel.setText(" ");
            
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // Validate inputs
            if (currentPassword.isEmpty()) {
                showPasswordError("Current password cannot be empty");
                return;
            }
            if (newPassword.isEmpty()) {
                showPasswordError("New password cannot be empty");
                return;
            }
            if (confirmPassword.isEmpty()) {
                showPasswordError("Please confirm your new password");
                return;
            }
            
            // Check password confirmation
            if (!newPassword.equals(confirmPassword)) {
                showPasswordError("New password and confirmation do not match");
                return;
            }
            
            // Validate password format
            if (!ValidationUtil.isValidPassword(newPassword)) {
                showPasswordError("Password must be at least 8 characters with letters and numbers");
                return;
            }
            
            // Change password
            boolean success = eventHub.changeCurrentUserPassword(currentPassword, newPassword);
            
            if (success) {
                passwordStatusLabel.setText("Password changed successfully!");
                passwordStatusLabel.setForeground(Color.GREEN);
                
                // Clear password fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                
                // Clear the status after 3 seconds
                Timer timer = new Timer(3000, e -> passwordStatusLabel.setText(" "));
                timer.setRepeats(false);
                timer.start();
            } else {
                showPasswordError("Failed to change password. Current password may be incorrect.");
            }
            
        } catch (Exception e) {
            showPasswordError("Error changing password: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
        
        // Clear the error after 5 seconds
        Timer timer = new Timer(5000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showPasswordError(String message) {
        passwordStatusLabel.setText(message);
        passwordStatusLabel.setForeground(Color.RED);
        
        // Clear the error after 5 seconds
        Timer timer = new Timer(5000, e -> passwordStatusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Refreshes the profile data from the current user
     */
    public void refreshProfile() {
        loadCurrentProfile();
        statusLabel.setText(" ");
        passwordStatusLabel.setText(" ");
    }
}