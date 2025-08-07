// =============================================================================
// MAIN APPLICATION ENTRY POINT
// =============================================================================

package com.campuseventhub;

import com.campuseventhub.gui.LoginFrame;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.config.ApplicationConfig;

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
}
