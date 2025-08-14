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
    protected static final int DEFAULT_WINDOW_WIDTH = 1024;
    protected static final int DEFAULT_WINDOW_HEIGHT = 768;
    
    protected EventHub eventHub;
    protected JMenuBar menuBar;
    protected JMenu userMenu;
    protected JMenu notificationMenu;

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
        setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }
    
    /**
     * Creates the common menu bar with user actions and notifications
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // Notifications Menu
        notificationMenu = new JMenu("Notifications");
        updateNotificationBadge();
        
        JMenuItem viewNotificationsItem = new JMenuItem("View All Notifications");
        viewNotificationsItem.addActionListener(e -> showNotificationsDialog());
        notificationMenu.add(viewNotificationsItem);
        
        JMenuItem clearNotificationsItem = new JMenuItem("Clear All Notifications");
        clearNotificationsItem.addActionListener(e -> clearNotifications());
        notificationMenu.add(clearNotificationsItem);
        
        // User Menu
        userMenu = new JMenu("User");
        if (eventHub.isUserLoggedIn()) {
            userMenu.setText("User (" + eventHub.getCurrentUser().getUsername() + ")");
        }
        

        
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> performLogout());
        userMenu.add(logoutItem);
        
        menuBar.add(notificationMenu);
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
    
    /**
     * Updates the notification menu badge with unread count
     */
    protected void updateNotificationBadge() {
        if (notificationMenu != null && eventHub.isUserLoggedIn()) {
            java.util.List<com.campuseventhub.model.notification.Notification> notifications = 
                eventHub.getCurrentUserNotifications();
            int unreadCount = notifications.size();
            
            if (unreadCount > 0) {
                notificationMenu.setText("Notifications (" + unreadCount + ")");
                notificationMenu.setForeground(Color.RED);
            } else {
                notificationMenu.setText("Notifications");
                notificationMenu.setForeground(Color.BLACK);
            }
        }
    }
    
    /**
     * Shows all notifications in a dialog
     */
    protected void showNotificationsDialog() {
        if (!eventHub.isUserLoggedIn()) {
            return;
        }
        
        java.util.List<com.campuseventhub.model.notification.Notification> notifications = 
            eventHub.getCurrentUserNotifications();
        
        if (notifications.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No notifications to display.", 
                "Notifications", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder notificationText = new StringBuilder();
        notificationText.append("=== YOUR NOTIFICATIONS ===\n\n");
        
        for (com.campuseventhub.model.notification.Notification notification : notifications) {
            notificationText.append("• ").append(notification.getType().getDisplayName()).append("\n");
            notificationText.append("  ").append(notification.getMessage()).append("\n");
            notificationText.append("  Time: ").append(notification.getCreatedAt().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        }
        
        JTextArea textArea = new JTextArea(notificationText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Your Notifications", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Clears all notifications for the current user
     */
    protected void clearNotifications() {
        // For now, just show a message. In a real implementation, 
        // we would mark notifications as read in the service
        JOptionPane.showMessageDialog(this, 
            "All notifications have been cleared.", 
            "Notifications Cleared", 
            JOptionPane.INFORMATION_MESSAGE);
        updateNotificationBadge();
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

