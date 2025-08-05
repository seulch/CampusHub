// =============================================================================
// COMPONENT FACTORY
// =============================================================================

package com.campuseventhub.gui.common;

import javax.swing.*;

/**
 * Factory for creating pre-configured Swing components used across the GUI.
 *
 * Implementation Details:
 * - Standard button and label creation
 * - Consistent styling for forms and dialogs
 * - Utility methods for common component setups
 */
public final class ComponentFactory {
    private ComponentFactory() {
        // Prevent instantiation
    }

    public static JButton createPrimaryButton(String text) {
        // TODO: Apply consistent styling for primary action buttons
        return new JButton(text);
    }

    public static JLabel createHeadingLabel(String text) {
        // TODO: Apply consistent styling for headings
        return new JLabel(text);
    }
}
