package com.campuseventhub.gui.admin;

import com.campuseventhub.model.user.User;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminUserManagementPanel extends JPanel {
    private EventHub eventHub;
    private DefaultListModel<String> usersListModel;
    private JList<String> usersList;
    private ActionListener onViewUser;
    private ActionListener onApproveUser;
    private ActionListener onSuspendUser;
    
    public AdminUserManagementPanel(EventHub eventHub) {
        this.eventHub = eventHub;
        this.usersListModel = new DefaultListModel<>();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = ComponentFactory.createHeadingLabel("User Management");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        usersList = new JList<>(usersListModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setPreferredSize(new Dimension(600, 400));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshUsersBtn = ComponentFactory.createStandardButton("Refresh");
        JButton viewUserBtn = ComponentFactory.createStandardButton("View User");
        JButton pendingApprovalsBtn = ComponentFactory.createStandardButton("Pending Approvals");
        JButton approveUserBtn = ComponentFactory.createPrimaryButton("Approve User");
        JButton suspendUserBtn = ComponentFactory.createStandardButton("Suspend User");
        
        refreshUsersBtn.addActionListener(e -> loadUsersData());
        viewUserBtn.addActionListener(e -> {
            if (onViewUser != null) onViewUser.actionPerformed(e);
        });
        pendingApprovalsBtn.addActionListener(e -> loadPendingApprovals());
        approveUserBtn.addActionListener(e -> {
            if (onApproveUser != null) onApproveUser.actionPerformed(e);
        });
        suspendUserBtn.addActionListener(e -> {
            if (onSuspendUser != null) onSuspendUser.actionPerformed(e);
        });
        
        buttonsPanel.add(refreshUsersBtn);
        buttonsPanel.add(viewUserBtn);
        buttonsPanel.add(pendingApprovalsBtn);
        buttonsPanel.add(approveUserBtn);
        buttonsPanel.add(suspendUserBtn);
        
        add(usersScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    public void loadUsersData() {
        usersListModel.clear();
        List<User> users = eventHub.getAllUsers();
        for (User user : users) {
            usersListModel.addElement(user.getUsername() + " (" + user.getRole() + ") - " + user.getStatus());
        }
    }
    
    public void loadPendingApprovals() {
        usersListModel.clear();
        List<User> pendingUsers = eventHub.getPendingUserApprovals();
        
        if (pendingUsers.isEmpty()) {
            usersListModel.addElement("No users pending approval.");
        } else {
            usersListModel.addElement("=== USERS PENDING APPROVAL ===");
            for (User user : pendingUsers) {
                String userInfo = String.format("ID: %s - %s %s (%s) - %s - Email: %s", 
                    user.getUserId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getRole().getDisplayName(),
                    user.getEmail()
                );
                usersListModel.addElement(userInfo);
            }
        }
    }
    
    public String getSelectedUser() {
        return usersList.getSelectedValue();
    }
    
    public void setOnViewUser(ActionListener listener) {
        this.onViewUser = listener;
    }
    
    public void setOnApproveUser(ActionListener listener) {
        this.onApproveUser = listener;
    }
    
    public void setOnSuspendUser(ActionListener listener) {
        this.onSuspendUser = listener;
    }
}