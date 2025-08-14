package com.campuseventhub.gui;

import com.campuseventhub.gui.common.ComponentFactory;
import org.junit.jupiter.api.Test;
import javax.swing.JButton;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that warning button text is black (readable on orange background)
 */
public class ButtonTextColorTest {

    @Test
    void testWarningButtonTextColor() {
        JButton warningButton = ComponentFactory.createWarningButton("Test Button");
        
        // Warning button should have black text for readability on orange background
        assertEquals(Color.BLACK, warningButton.getForeground());
        assertEquals(ComponentFactory.WARNING_COLOR, warningButton.getBackground());
    }
    
    @Test
    void testOtherButtonTextColors() {
        // Primary button should have white text
        JButton primaryButton = ComponentFactory.createPrimaryButton("Primary");
        assertEquals(Color.WHITE, primaryButton.getForeground());
        
        // Success button should have white text
        JButton successButton = ComponentFactory.createSuccessButton("Success");
        assertEquals(Color.WHITE, successButton.getForeground());
        
        // Error button should have white text
        JButton errorButton = ComponentFactory.createErrorButton("Error");
        assertEquals(Color.WHITE, errorButton.getForeground());
    }
}