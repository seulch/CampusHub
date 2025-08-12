// =============================================================================
// COMPONENT FACTORY
// =============================================================================

package com.campuseventhub.gui.common;

import javax.swing.*;
import java.awt.*;
import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.service.EventHub;

public final class ComponentFactory {
    private ComponentFactory() {
    }

    public static JButton createPrimaryButton(String text) {
        return new JButton(text);
    }

    public static JLabel createHeadingLabel(String text) {
        return new JLabel(text);
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
