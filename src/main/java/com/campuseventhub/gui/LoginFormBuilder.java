package com.campuseventhub.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Builder class for creating the login form UI components and layout.
 * Follows the Builder pattern to separate UI construction from the main frame logic.
 */
public class LoginFormBuilder {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox rememberUsernameCheckbox;
    
    /**
     * Creates and initializes all form components
     */
    public LoginFormBuilder createComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        statusLabel = new JLabel("Welcome to Campus EventHub");
        rememberUsernameCheckbox = new JCheckBox("Remember Username");
        return this;
    }
    
    /**
     * Applies the layout to the parent container
     */
    public LoginFormBuilder applyLayout(Container parent) {
        parent.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        parent.add(new JLabel("Campus EventHub"), gbc);
        
        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        parent.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        parent.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        parent.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        parent.add(passwordField, gbc);
        
        // Remember checkbox
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        parent.add(rememberUsernameCheckbox, gbc);
        
        // Buttons
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        parent.add(loginButton, gbc);
        
        gbc.gridx = 1;
        parent.add(registerButton, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        parent.add(statusLabel, gbc);
        
        return this;
    }
    
    // Getters for accessing the created components
    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getLoginButton() { return loginButton; }
    public JButton getRegisterButton() { return registerButton; }
    public JLabel getStatusLabel() { return statusLabel; }
    public JCheckBox getRememberUsernameCheckbox() { return rememberUsernameCheckbox; }
}