// =============================================================================
// BASE FRAME FOR GUI DASHBOARDS
// =============================================================================

package com.campuseventhub.gui.common;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.campuseventhub.gui.LoginFrame;
import com.campuseventhub.service.EventHub;

/**
 * Common base frame that provides shared window configuration for all
 * dashboard windows.  Subclasses can optionally override the hook methods
 * to perform component creation and event binding.
 */
public abstract class BaseFrame extends JFrame {
    protected EventHub eventHub;
    protected JMenuBar menuBar;
    protected JMenu userMenu;

    /**
     * Constructs the frame with a provided window title and applies common
     * configuration such as size, close operation and centering on screen.
     *
     * @param title window title
     */
    protected BaseFrame(String title) {
        super(title);
        this.eventHub = EventHub.getInstance();
        configureWindow();
        createMenuBar();
    }

    /**
     * Applies common frame settings used by all dashboards.
     */
    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
    }
    
    /**
     * Creates the common menu bar with user actions
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // User Menu
        userMenu = new JMenu("User");
        if (eventHub.isUserLoggedIn()) {
            userMenu.setText("User (" + eventHub.getCurrentUser().getUsername() + ")");
        }
        

        
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> performLogout());
        userMenu.add(logoutItem);
        
        menuBar.add(Box.createHorizontalGlue()); // Push user menu to right
        menuBar.add(userMenu);
        
        // Help Menu (non-intrusive)
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "<html><b>Campus Event Hub</b><br/>" +
                "Version 1.0<br/>" +
                "Java: " + System.getProperty("java.version") + "<br/>" +
                "User: " + (eventHub.getCurrentUser() != null
                        ? eventHub.getCurrentUser().getUsername() : "Guest") +
                "</html>",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        ));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
    
    /**
     * Handles user logout and returns to login screen
     */
    protected void performLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            eventHub.logoutCurrentUser();
            dispose();
            
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }

    // ---------------------------------------------------------------------
    // Hook methods for subclasses
    // ---------------------------------------------------------------------

    /**
     * Allows subclasses to create and arrange their components.  This method
     * is invoked by {@link #init()} and has an empty default implementation.
     */
    protected void initializeComponents() {
        // no-op default
    }

    /**
     * Allows subclasses to register event listeners.  This method is invoked
     * by {@link #init()} and has an empty default implementation.
     */
    protected void registerListeners() {
        // no-op default
    }

    /**
     * Template method that invokes the lifecycle hook methods.  Subclasses
     * should call this at the end of their constructors once their fields are
     * initialized.
     */
    protected final void init() {
        initializeComponents();
        registerListeners();
    }
}

