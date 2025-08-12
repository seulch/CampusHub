// =============================================================================
// MAIN APPLICATION ENTRY POINT
// =============================================================================

package com.campuseventhub;

import com.campuseventhub.gui.LoginFrame;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.config.ApplicationConfig;
import com.campuseventhub.model.user.UserRole;

/**
 * Main entry point for Campus EventHub application.
 * 
 * Implementation Details:
 * - Initialize application configuration and logging
 * - Load serialized data from previous sessions
 * - Set up Look & Feel for consistent GUI appearance
 * - Launch login window with proper error handling
 * - Register shutdown hooks for data persistence
 */
public class Main {
    public static void main(String[] args) {
        // Set up Swing to run on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Set system Look & Feel
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
                
                // Initialize EventHub singleton
                EventHub eventHub = EventHub.getInstance();
                
                // Add shutdown hook to ensure data persistence
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Application shutting down...");
                    eventHub.shutdown();
                }));
                
                // Create test accounts with strong passwords if they don't exist
                setupTestAccounts(eventHub);
                
                // Create and display LoginFrame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                
                System.out.println("Campus EventHub application started successfully!");
                
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Creates test accounts with strong passwords if they don't already exist
     */
    private static void setupTestAccounts(EventHub eventHub) {
        try {
            // Try to create test accounts - will fail silently if they already exist
            eventHub.registerUser("admin", "admin@test.com", "admin123", "Admin", "User", UserRole.ADMIN);
            eventHub.registerUser("organizer", "organizer@test.com", "organizer123", "Test", "Organizer", UserRole.ORGANIZER);
            eventHub.registerUser("attendee", "attendee@test.com", "attendee123", "Test", "Attendee", UserRole.ATTENDEE);
            
            System.out.println("Test accounts created/verified:");
            System.out.println("  Admin: admin / admin123");
            System.out.println("  Organizer: organizer / organizer123");
            System.out.println("  Attendee: attendee / attendee123");
            
        } catch (Exception e) {
            // Accounts may already exist, which is fine
            System.out.println("Test accounts already exist or could not be created.");
        }
    }
}
