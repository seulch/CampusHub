package com.campuseventhub.gui.admin;

import com.campuseventhub.gui.common.BaseFrame;
import com.campuseventhub.gui.common.ProfileEditingPanel;
import com.campuseventhub.model.user.Admin;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends BaseFrame {
    private Admin admin;
    private JTabbedPane mainTabbedPane;
    private AdminOverviewPanel overviewPanel;
    private AdminUserManagementPanel userManagementPanel;
    private AdminVenueManagementPanel venueManagementPanel;
    private AdminSystemStatsPanel systemStatsPanel;
    private ProfileEditingPanel profilePanel;
    private AdminActionHandler actionHandler;
    
    public AdminDashboard(Admin admin) {
        super("Campus EventHub - Administration");
        this.admin = admin;
        this.actionHandler = new AdminActionHandler(eventHub, admin, this);
        
        initializeComponents();
        setupEventHandlers();
        loadData();
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        mainTabbedPane = new JTabbedPane();
        
        systemStatsPanel = new AdminSystemStatsPanel(eventHub);
        overviewPanel = new AdminOverviewPanel(eventHub, admin, this, mainTabbedPane, systemStatsPanel);
        userManagementPanel = new AdminUserManagementPanel(eventHub);
        venueManagementPanel = new AdminVenueManagementPanel(eventHub, this);
        profilePanel = new ProfileEditingPanel(eventHub);
        
        mainTabbedPane.addTab("Overview", overviewPanel);
        mainTabbedPane.addTab("User Management", userManagementPanel);
        mainTabbedPane.addTab("Venue Management", venueManagementPanel);
        mainTabbedPane.addTab("System Stats", systemStatsPanel);
        mainTabbedPane.addTab("My Profile", profilePanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        userManagementPanel.setOnViewUser(e -> 
            actionHandler.viewUserDetails(userManagementPanel.getSelectedUser()));
        
        userManagementPanel.setOnApproveUser(e -> {
            if (actionHandler.approveSelectedUser(userManagementPanel.getSelectedUser())) {
                userManagementPanel.loadPendingApprovals();
                overviewPanel.refreshMetrics();
            }
        });
        
        userManagementPanel.setOnSuspendUser(e -> {
            if (actionHandler.suspendSelectedUser(userManagementPanel.getSelectedUser())) {
                userManagementPanel.loadUsersData();
                overviewPanel.refreshMetrics();
            }
        });
    }
    
    private void loadData() {
        userManagementPanel.loadUsersData();
        venueManagementPanel.loadVenuesData();
    }
}