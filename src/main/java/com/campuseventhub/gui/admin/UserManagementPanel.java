// =============================================================================
// USER MANAGEMENT PANEL
// =============================================================================

package com.campuseventhub.gui.admin;

import com.campuseventhub.model.user.User;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for administrators to manage user accounts.
 *
 * Implementation Details:
 * - User table with search and filter
 * - Actions for approve, suspend, or delete
 * - Bulk operation support
 * - Display of user activity metrics
 */
public class UserManagementPanel extends JPanel {
    private EventHub eventHub;
    private DefaultListModel<String> usersListModel;
    private JList<String> usersList;
    private JButton refreshUsersBtn;
    private JButton viewUserBtn;
    
    public UserManagementPanel() {
        this.eventHub = EventHub.getInstance();
        initializeComponents();
        loadUsers();
        registerListeners();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("User Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Users list
        usersListModel = new DefaultListModel<>();
        usersList = new JList<>(usersListModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane usersScrollPane = new JScrollPane(usersList);
        usersScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        refreshUsersBtn = new JButton("Refresh");
        viewUserBtn = new JButton("View Details");
        
        buttonsPanel.add(refreshUsersBtn);
        buttonsPanel.add(viewUserBtn);
        
        add(usersScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        usersListModel.clear();
        List<User> users = eventHub.getAllUsers();
        for (User user : users) {
            usersListModel.addElement(user.getUsername() + " (" + user.getRole() + ") - " + user.getStatus());
        }
    }

    private void registerListeners() {
        refreshUsersBtn.addActionListener(e -> loadUsers());
        viewUserBtn.addActionListener(e -> viewUserDetails(usersList.getSelectedValue()));
    }
    
    private void viewUserDetails(String selectedUser) {
        if (selectedUser != null) {
            JOptionPane.showMessageDialog(this, "User Details:\n" + selectedUser, "User Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void refreshData() {
        loadUsers();
    }
}
