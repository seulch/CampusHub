package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.notification.Notification;
import com.campuseventhub.model.notification.NotificationType;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for displaying and managing attendee notifications
 */
public class AttendeeNotificationPanel extends JPanel {
    private EventHub eventHub;
    private String attendeeId;
    private DefaultListModel<String> notificationsListModel;
    private JList<String> notificationsList;
    private JLabel notificationCountLabel;
    private JButton refreshBtn;
    private JButton markAllReadBtn;
    private JButton clearAllBtn;
    private JTextArea notificationDetailsArea;
    private List<Notification> currentNotifications;
    
    // Action listeners
    private ActionListener onNotificationSelected;
    private ActionListener onMarkAsRead;
    private ActionListener onClearNotifications;
    
    public AttendeeNotificationPanel(EventHub eventHub, String attendeeId) {
        this.eventHub = eventHub;
        this.attendeeId = attendeeId;
        this.notificationsListModel = new DefaultListModel<>();
        
        initializeComponents();
        loadNotifications();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("📧 My Notifications"));
        
        // Header panel with notification count and actions
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Action buttons panel
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        notificationCountLabel = ComponentFactory.createBoldLabel("Loading notifications...");
        notificationCountLabel.setForeground(new Color(0, 100, 200));
        
        refreshBtn = ComponentFactory.createPrimaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadNotifications());
        
        panel.add(notificationCountLabel, BorderLayout.WEST);
        panel.add(refreshBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Notifications list
        notificationsList = new JList<>(notificationsListModel);
        notificationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationsList.setCellRenderer(new NotificationListCellRenderer());
        notificationsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showNotificationDetails();
                if (onNotificationSelected != null) {
                    onNotificationSelected.actionPerformed(null);
                }
            }
        });
        
        JScrollPane notificationsScrollPane = new JScrollPane(notificationsList);
        notificationsScrollPane.setPreferredSize(new Dimension(0, 200));
        
        // Details area
        notificationDetailsArea = ComponentFactory.createStandardTextArea();
        notificationDetailsArea.setRows(8);
        notificationDetailsArea.setText("Select a notification to view details...");
        notificationDetailsArea.setBackground(Color.WHITE);
        notificationDetailsArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JScrollPane detailsScrollPane = new JScrollPane(notificationDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Notification Details"));
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, notificationsScrollPane, detailsScrollPane);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(200);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        markAllReadBtn = ComponentFactory.createSuccessButton("✓ Mark All Read");
        markAllReadBtn.addActionListener(e -> markAllNotificationsAsRead());
        
        clearAllBtn = ComponentFactory.createWarningButton("🗑 Clear All");
        clearAllBtn.addActionListener(e -> clearAllNotifications());
        
        panel.add(markAllReadBtn);
        panel.add(clearAllBtn);
        
        return panel;
    }
    
    public void loadNotifications() {
        notificationsListModel.clear();
        currentNotifications = eventHub.getUserNotifications(attendeeId);
        
        if (currentNotifications == null || currentNotifications.isEmpty()) {
            notificationsListModel.addElement("📭 No notifications available");
            updateNotificationCount(0, 0);
        } else {
            int unreadCount = 0;
            for (Notification notification : currentNotifications) {
                String prefix = notification.getSentAt() == null ? "🔵 " : "⚫ ";
                String typeIcon = getNotificationTypeIcon(notification.getType());
                String timeStr = notification.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
                
                String listItem = String.format("%s%s [%s] %s", 
                    prefix, typeIcon, timeStr, 
                    truncateMessage(notification.getMessage(), 60));
                    
                notificationsListModel.addElement(listItem);
                
                if (notification.getSentAt() == null) {
                    unreadCount++;
                }
            }
            updateNotificationCount(currentNotifications.size(), unreadCount);
        }
        
        // Update button states
        boolean hasNotifications = currentNotifications != null && !currentNotifications.isEmpty();
        markAllReadBtn.setEnabled(hasNotifications);
        clearAllBtn.setEnabled(hasNotifications);
        
        notificationDetailsArea.setText("Select a notification to view details...");
    }
    
    private void updateNotificationCount(int total, int unread) {
        String countText = String.format("📧 Total: %d | 🔵 Unread: %d", total, unread);
        notificationCountLabel.setText(countText);
        
        if (unread > 0) {
            notificationCountLabel.setForeground(new Color(200, 0, 0)); // Red for unread
        } else {
            notificationCountLabel.setForeground(new Color(0, 150, 0)); // Green for all read
        }
    }
    
    private void showNotificationDetails() {
        int selectedIndex = notificationsList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentNotifications.size()) {
            Notification notification = currentNotifications.get(selectedIndex);
            
            StringBuilder details = new StringBuilder();
            details.append("=== NOTIFICATION DETAILS ===\n\n");
            details.append("Type: ").append(notification.getType().getDisplayName()).append("\n");
            details.append("Status: ").append(notification.getSentAt() == null ? "Unread" : "Read").append("\n");
            details.append("Received: ").append(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            if (notification.getSentAt() != null) {
                details.append("Read: ").append(notification.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            }
            details.append("\n--- MESSAGE ---\n\n");
            details.append(notification.getMessage());
            
            notificationDetailsArea.setText(details.toString());
            notificationDetailsArea.setCaretPosition(0);
            
            // Auto-mark as read when viewed
            if (notification.getSentAt() == null) {
                eventHub.getNotificationService().markNotificationAsRead(notification.getNotificationId());
                SwingUtilities.invokeLater(() -> loadNotifications()); // Refresh to show updated status
            }
        }
    }
    
    private void markAllNotificationsAsRead() {
        if (currentNotifications != null) {
            for (Notification notification : currentNotifications) {
                if (notification.getSentAt() == null) {
                    eventHub.getNotificationService().markNotificationAsRead(notification.getNotificationId());
                }
            }
            loadNotifications();
            JOptionPane.showMessageDialog(this, "All notifications marked as read!", 
                "Notifications Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void clearAllNotifications() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear all notifications?\nThis action cannot be undone.", 
            "Clear All Notifications", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            eventHub.getNotificationService().clearUserNotifications(attendeeId);
            loadNotifications();
            JOptionPane.showMessageDialog(this, "All notifications cleared!", 
                "Notifications Cleared", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private String getNotificationTypeIcon(NotificationType type) {
        switch (type) {
            case EVENT_REMINDER:
                return "⏰";
            case EVENT_REGISTRATION_CONFIRMATION:
                return "✅";
            case EVENT_UPDATE:
                return "📝";
            case EVENT_CANCELLATION:
                return "❌";
            case WAITLIST_PROMOTION:
                return "⬆️";
            case WAITLIST_POSITION_UPDATE:
                return "🔄";
            case WAITLIST_REGISTRATION:
                return "⏳";
            case SYSTEM_ANNOUNCEMENT:
                return "📢";
            case SYSTEM_ALERT:
                return "⚠️";
            default:
                return "📧";
        }
    }
    
    private String truncateMessage(String message, int maxLength) {
        if (message == null) return "";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength) + "...";
    }
    
    public String getSelectedNotification() {
        return notificationsList.getSelectedValue();
    }
    
    public Notification getSelectedNotificationObject() {
        int selectedIndex = notificationsList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentNotifications.size()) {
            return currentNotifications.get(selectedIndex);
        }
        return null;
    }
    
    // Setters for action listeners
    public void setOnNotificationSelected(ActionListener listener) {
        this.onNotificationSelected = listener;
    }
    
    public void setOnMarkAsRead(ActionListener listener) {
        this.onMarkAsRead = listener;
    }
    
    public void setOnClearNotifications(ActionListener listener) {
        this.onClearNotifications = listener;
    }
    
    /**
     * Custom cell renderer for notification list
     */
    private static class NotificationListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String text = value.toString();
            
            // Style unread notifications differently
            if (text.startsWith("🔵")) {
                setFont(getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    setBackground(new Color(240, 248, 255)); // Light blue background
                }
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            return this;
        }
    }
}