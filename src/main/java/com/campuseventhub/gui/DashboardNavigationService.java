package com.campuseventhub.gui;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.gui.admin.AdminDashboard;
import com.campuseventhub.gui.organizer.OrganizerDashboard;
import com.campuseventhub.gui.attendee.AttendeeDashboard;
import javax.swing.*;

/**
 * Service for handling navigation to appropriate dashboards based on user roles.
 * Implements the Strategy pattern for role-based dashboard routing.
 */
public class DashboardNavigationService {
    
    private final JLabel statusLabel;
    private final JFrame parentFrame;
    
    public DashboardNavigationService(JLabel statusLabel, JFrame parentFrame) {
        this.statusLabel = statusLabel;
        this.parentFrame = parentFrame;
    }
    
    /**
     * Opens the appropriate dashboard for the authenticated user
     */
    public boolean openDashboardForUser(User user) {
        try {
            switch (user.getRole()) {
                case ADMIN:
                    AdminDashboard adminDashboard = new AdminDashboard((Admin) user);
                    adminDashboard.setVisible(true);
                    break;
                case ORGANIZER:
                    OrganizerDashboard organizerDashboard = new OrganizerDashboard((Organizer) user);
                    organizerDashboard.setVisible(true);
                    break;
                case ATTENDEE:
                    AttendeeDashboard attendeeDashboard = new AttendeeDashboard((Attendee) user);
                    attendeeDashboard.setVisible(true);
                    break;
                default:
                    statusLabel.setText("Unknown user role: " + user.getRole());
                    return false;
            }
            
            // Close login window after successful dashboard opening
            parentFrame.dispose();
            return true;
        } catch (Exception e) {
            statusLabel.setText("Error opening dashboard: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Opens the registration frame
     */
    public void openRegistrationFrame() {
        RegistrationFrame registrationFrame = new RegistrationFrame();
        registrationFrame.setVisible(true);
    }
}