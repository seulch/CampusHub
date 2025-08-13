// =============================================================================
// GUI FRAMEWORK - LOGIN FRAME
// =============================================================================

package com.campuseventhub.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.admin.AdminDashboard;
import com.campuseventhub.gui.organizer.OrganizerDashboard;
import com.campuseventhub.gui.attendee.AttendeeDashboard;

/**
 * Simplified login window using composition of specialized services.
 * 
 * Implementation Details:
 * - Delegates UI construction to LoginFormBuilder
 * - Delegates authentication to LoginController
 * - Delegates navigation to DashboardNavigationService
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox rememberUsernameCheckbox;
    
    // Specialized services
    private LoginController loginController;
    private DashboardNavigationService navigationService;
    private LoginFormBuilder formBuilder;
    
    public LoginFrame() {
        setTitle("Campus EventHub - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Center on screen
        
        initializeComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Use form builder to create and layout components
        formBuilder = new LoginFormBuilder()
            .createComponents()
            .applyLayout(this);
        
        // Get references to components
        usernameField = formBuilder.getUsernameField();
        passwordField = formBuilder.getPasswordField();
        loginButton = formBuilder.getLoginButton();
        registerButton = formBuilder.getRegisterButton();
        statusLabel = formBuilder.getStatusLabel();
        rememberUsernameCheckbox = formBuilder.getRememberUsernameCheckbox();
        
        // Initialize specialized services
        loginController = new LoginController(statusLabel, passwordField);
        navigationService = new DashboardNavigationService(statusLabel, this);
    }
    
    private void setupEventHandlers() {
        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> navigationService.openRegistrationFrame());
        
        // Handle Enter key in password field
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        User authenticatedUser = loginController.performLogin(username, password);
        if (authenticatedUser != null) {
            navigationService.openDashboardForUser(authenticatedUser);
        }
    }

}