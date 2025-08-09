// =============================================================================
// ADMIN DASHBOARD
// =============================================================================

package com.campuseventhub.gui.admin;

import com.campuseventhub.gui.common.BaseFrame;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.User;
import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.model.event.Event;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Administrative dashboard for system management.
 * 
 * Implementation Details:
 * - System overview with key metrics
 * - User management interface
 * - Event approval workflows
 * - Venue management system
 * - Report generation tools
 * - System configuration panel
 */
public class AdminDashboard extends BaseFrame {
    private Admin admin;
    private JTabbedPane mainTabbedPane;
    private JPanel overviewPanel;
    private JPanel userManagementPanel;
    private JPanel venueManagementPanel;
    private JPanel systemStatsPanel;
    
    // Data models for tables
    private DefaultListModel<String> usersListModel;
    private DefaultListModel<String> venuesListModel;
    
    public AdminDashboard(Admin admin) {
        super("Campus EventHub - Administration");
        this.admin = admin;
        
        initializeComponents();
        loadData();
        registerListeners();
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();
        
        // Create panels
        createOverviewPanel();
        createUserManagementPanel();
        createVenueManagementPanel();
        createSystemStatsPanel();
        
        // Add tabs
        mainTabbedPane.addTab("Overview", overviewPanel);
        mainTabbedPane.addTab("User Management", userManagementPanel);
        mainTabbedPane.addTab("Venue Management", venueManagementPanel);
        mainTabbedPane.addTab("System Stats", systemStatsPanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void createOverviewPanel() {
        overviewPanel = new JPanel(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getFirstName() + " " + admin.getLastName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        overviewPanel.add(welcomeLabel, BorderLayout.NORTH);
        
        // Create overview content with system metrics
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add system info panels
        contentPanel.add(createSystemMetricsPanel());
        contentPanel.add(createQuickActionsPanel());
        contentPanel.add(createRecentActivityPanel());
        contentPanel.add(createSystemHealthPanel());
        
        overviewPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    // System Metrics Panel
    private JPanel createSystemMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Metrics"));
        
        JLabel metricsLabel = new JLabel("<html><body>" +
            "<b>Total Users:</b> " + eventHub.getAllUsers().size() + "<br>" +
            "<b>Total Venues:</b> " + eventHub.listVenues().size() + "<br>" +
            "<b>System Status:</b> <font color='green'>Online</font>" +
            "</body></html>");
        // hopefully this doesnt come out messed up
        panel.add(metricsLabel, BorderLayout.CENTER);
        return panel;
    }
    
    // Quick Actions Panel
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton addVenueBtn = new JButton("Add New Venue");
        JButton viewUsersBtn = new JButton("View All Users");
        JButton systemReportBtn = new JButton("Generate System Report");
        
        addVenueBtn.addActionListener(e -> showAddVenueDialog());
        viewUsersBtn.addActionListener(e -> mainTabbedPane.setSelectedIndex(1));
        systemReportBtn.addActionListener(e -> generateSystemReport());
        
        panel.add(addVenueBtn);
        panel.add(viewUsersBtn);
        panel.add(systemReportBtn);
        
        return panel;
    }
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        
        JTextArea activityArea = new JTextArea("• System started\n• Admin logged in\n• Dashboard loaded");
        activityArea.setEditable(false);
        activityArea.setBackground(getBackground());
        
        panel.add(new JScrollPane(activityArea), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSystemHealthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Health"));
        
        // Get memory usage ---->
        // Memory Used: Actual memory currently being used by the application
        // Memory Total: Currently allocated heap space
        // Memory Max: Maximum heap size the JVM can use
        // Real-time: Updates when you refresh or navigate
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        JLabel healthLabel = new JLabel("<html><body>" +
            "<b>Status:</b> <font color='green'>All systems operational</font><br>" +
            "<b>Memory Used:</b> " + (usedMemory / 1024 / 1024) + "MB<br>" +
            "<b>Memory Total:</b> " + (totalMemory / 1024 / 1024) + "MB<br>" +
            "<b>Memory Max:</b> " + (maxMemory / 1024 / 1024) + "MB" +
            "</body></html>");
        
        panel.add(healthLabel, BorderLayout.CENTER);
        return panel;
    }
    // just a thought but I dont think I needed to implement this.. done and works anyways
    
    private void createUserManagementPanel() {
        userManagementPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("User Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userManagementPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Users list
        usersListModel = new DefaultListModel<>();
        JList<String> usersList = new JList<>(usersListModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshUsersBtn = new JButton("Refresh");
        JButton viewUserBtn = new JButton("View Details");
        
        refreshUsersBtn.addActionListener(e -> loadUsersData());
        viewUserBtn.addActionListener(e -> viewUserDetails(usersList.getSelectedValue()));
        
        buttonsPanel.add(refreshUsersBtn);
        buttonsPanel.add(viewUserBtn);
        
        userManagementPanel.add(usersScrollPane, BorderLayout.CENTER);
        userManagementPanel.add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void createVenueManagementPanel() {
        venueManagementPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Venue Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        venueManagementPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Venues list
        venuesListModel = new DefaultListModel<>();
        JList<String> venuesList = new JList<>(venuesListModel);
        venuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane venuesScrollPane = new JScrollPane(venuesList);
        venuesScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton addVenueBtn = new JButton("Add Venue");
        JButton refreshVenuesBtn = new JButton("Refresh");
        
        addVenueBtn.addActionListener(e -> showAddVenueDialog());
        refreshVenuesBtn.addActionListener(e -> loadVenuesData());
        
        buttonsPanel.add(addVenueBtn);
        buttonsPanel.add(refreshVenuesBtn);
        
        venueManagementPanel.add(venuesScrollPane, BorderLayout.CENTER);
        venueManagementPanel.add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void createSystemStatsPanel() {
        systemStatsPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("System Statistics", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        systemStatsPanel.add(titleLabel, BorderLayout.NORTH);
        
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane statsScrollPane = new JScrollPane(statsArea);
        systemStatsPanel.add(statsScrollPane, BorderLayout.CENTER);
        
        updateSystemStats(statsArea);
    }
    
    private void loadData() {
        loadUsersData();
        loadVenuesData();
    }
    
    private void loadUsersData() {
        usersListModel.clear();
        List<User> users = eventHub.getAllUsers();
        for (User user : users) {
            usersListModel.addElement(user.getUsername() + " (" + user.getRole() + ") - " + user.getStatus());
        }
    }
    
    private void loadVenuesData() {
        venuesListModel.clear();
        List<Venue> venues = eventHub.listVenues();
        for (Venue venue : venues) {
            venuesListModel.addElement(venue.getName() + " - Capacity: " + venue.getCapacity());
        }
    }
    
    private void showAddVenueDialog() {
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JTextField capacityField = new JTextField(10);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacityField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Venue", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String location = locationField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                
                if (!name.isEmpty() && !location.isEmpty() && capacity > 0) {
                    Venue venue = new Venue(name, location, capacity);
                    if (eventHub.addVenue(venue)) {
                        loadVenuesData();
                        JOptionPane.showMessageDialog(this, "Venue added successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add venue.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please fill all fields with valid data.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Capacity must be a valid number.");
            }
        }
    }
    
    private void viewUserDetails(String selectedUser) {
        if (selectedUser != null) {
            JOptionPane.showMessageDialog(this, "User Details:\n" + selectedUser, "User Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void generateSystemReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== CAMPUS EVENTHUB SYSTEM REPORT ===\n\n");
        report.append("Generated: ").append(new java.util.Date()).append("\n\n");
        report.append("USERS:\n");
        report.append("Total Users: ").append(eventHub.getAllUsers().size()).append("\n\n");
        report.append("VENUES:\n");
        report.append("Total Venues: ").append(eventHub.listVenues().size()).append("\n\n");
        
        JTextArea reportArea = new JTextArea(report.toString());
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "System Report", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateSystemStats(JTextArea statsArea) {
        StringBuilder stats = new StringBuilder();
        stats.append("=== SYSTEM STATISTICS ===\n\n");
        stats.append("User Statistics:\n");
        stats.append("- Total Users: ").append(eventHub.getAllUsers().size()).append("\n");
        stats.append("- Active Users: ").append(eventHub.getAllUsers().stream().mapToInt(u -> u.isActive() ? 1 : 0).sum()).append("\n\n");
        
        stats.append("Venue Statistics:\n");
        stats.append("- Total Venues: ").append(eventHub.listVenues().size()).append("\n");
        stats.append("- Active Venues: ").append(eventHub.listVenues().stream().mapToInt(v -> v.isActive() ? 1 : 0).sum()).append("\n\n");
        
        statsArea.setText(stats.toString());
    }
}