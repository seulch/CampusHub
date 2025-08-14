// =============================================================================
// COMPONENT FACTORY
// =============================================================================

package com.campuseventhub.gui.common;

import javax.swing.*;
import java.awt.*;
import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.service.EventHub;

public final class ComponentFactory {
    // Standard fonts used throughout the application
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font LARGE_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);
    public static final Font TEXT_AREA_FONT = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font BOLD_LABEL_FONT = new Font("Arial", Font.BOLD, 12);
    
    // Standard colors
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);  // Steel Blue
    public static final Color SUCCESS_COLOR = new Color(34, 139, 34);   // Forest Green
    public static final Color WARNING_COLOR = new Color(255, 140, 0);   // Dark Orange
    public static final Color ERROR_COLOR = new Color(178, 34, 34);     // Fire Brick
    
    private ComponentFactory() {
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
    
    public static JButton createStandardButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        return button;
    }
    
    public static JButton createSuccessButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
    
    public static JButton createWarningButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(WARNING_COLOR);
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
    
    public static JButton createErrorButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(ERROR_COLOR);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    public static JLabel createHeadingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        return label;
    }
    
    public static JLabel createLargeTitleLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(LARGE_TITLE_FONT);
        return label;
    }
    
    public static JLabel createStandardLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }
    
    public static JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD_LABEL_FONT);
        return label;
    }
    
    public static JTextArea createStandardTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(TEXT_AREA_FONT);
        textArea.setEditable(false);
        return textArea;
    }
    
    public static boolean showAddVenueDialog(Component parent, Runnable onSuccess) {
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
        
        int result = JOptionPane.showConfirmDialog(parent, panel, "Add New Venue", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String location = locationField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                
                if (!name.isEmpty() && !location.isEmpty() && capacity > 0) {
                    Venue venue = new Venue(name, location, capacity);
                    EventHub eventHub = EventHub.getInstance();
                    if (eventHub.addVenue(venue)) {
                        onSuccess.run();
                        JOptionPane.showMessageDialog(parent, "Venue added successfully!");
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(parent, "Failed to add venue. It may already exist.");
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, "Please fill all fields with valid data.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Capacity must be a valid number.");
            }
        }
        return false;
    }
}
