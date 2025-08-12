package com.campuseventhub.gui.admin;

import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.User;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminActionHandler {
    private EventHub eventHub;
    private Admin admin;
    private JFrame parentFrame;
    
    public AdminActionHandler(EventHub eventHub, Admin admin, JFrame parentFrame) {
        this.eventHub = eventHub;
        this.admin = admin;
        this.parentFrame = parentFrame;
    }
    
    public void viewUserDetails(String selectedUser) {
        if (selectedUser != null) {
            JOptionPane.showMessageDialog(parentFrame, "User Details:\n" + selectedUser, 
                "User Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public boolean approveSelectedUser(String selectedUser) {
        if (selectedUser == null || selectedUser.contains("No users") || selectedUser.contains("===")) {
            JOptionPane.showMessageDialog(parentFrame, "Please select a user to approve!", 
                "No User Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int choice = JOptionPane.showConfirmDialog(parentFrame, 
            "Are you sure you want to approve this user?\n" + selectedUser, 
            "Confirm User Approval", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }
        
        try {
            String userId = selectedUser.split(" - ")[0].replace("ID: ", "");
            boolean approved = eventHub.approveUser(userId);
            
            if (approved) {
                JOptionPane.showMessageDialog(parentFrame, 
                    "User approved successfully! They can now log in.",
                    "User Approved", 
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Failed to approve user. The user may not exist or already be approved.",
                    "Approval Failed", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Error during approval: " + e.getMessage(),
                "Approval Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    public boolean suspendSelectedUser(String selectedUser) {
        if (selectedUser == null || selectedUser.contains("No users") || selectedUser.contains("===")) {
            JOptionPane.showMessageDialog(parentFrame, "Please select a user to suspend!", 
                "No User Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int choice = JOptionPane.showConfirmDialog(parentFrame, 
            "Are you sure you want to suspend this user?\n" + selectedUser, 
            "Confirm User Suspension", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }
        
        try {
            String userId = extractUserId(selectedUser);
            if (userId == null) {
                JOptionPane.showMessageDialog(parentFrame, "User not found!", 
                    "Suspension Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            boolean suspended = eventHub.suspendUser(userId);
            
            if (suspended) {
                JOptionPane.showMessageDialog(parentFrame, 
                    "User suspended successfully! They will no longer be able to log in.",
                    "User Suspended", 
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Failed to suspend user. The user may not exist or already be suspended.",
                    "Suspension Failed", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Error during suspension: " + e.getMessage(),
                "Suspension Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private String extractUserId(String selectedUser) {
        if (selectedUser.contains("ID: ")) {
            return selectedUser.split(" - ")[0].replace("ID: ", "");
        } else {
            String username = selectedUser.split(" \\(")[0];
            List<User> allUsers = eventHub.getAllUsers();
            User targetUser = allUsers.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
            
            return targetUser != null ? targetUser.getUserId() : null;
        }
    }
    
    public void showSystemReport(AdminSystemStatsPanel statsPanel) {
        String reportText = statsPanel.generateSystemReport();
        
        JTextArea reportArea = new JTextArea(reportText);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(parentFrame, scrollPane, "System Report", JOptionPane.INFORMATION_MESSAGE);
    }
}