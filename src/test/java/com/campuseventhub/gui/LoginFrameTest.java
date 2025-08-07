package com.campuseventhub.gui;

import org.junit.jupiter.api.Test;
import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoginFrameTest {
    @Test
    public void testLoginFrameCreation() {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            assertNotNull(frame);
            frame.setVisible(true);
            assertTrue(frame.isVisible());
            frame.dispose();
        });
    }
}