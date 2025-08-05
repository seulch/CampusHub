// =============================================================================
// BASE FRAME FOR GUI DASHBOARDS
// =============================================================================

package com.campuseventhub.gui.common;

import javax.swing.*;

/**
 * Common base frame that provides shared window configuration for all
 * dashboard windows.  Subclasses can optionally override the hook methods
 * to perform component creation and event binding.
 */
public abstract class BaseFrame extends JFrame {

    /**
     * Constructs the frame with a provided window title and applies common
     * configuration such as size, close operation and centering on screen.
     *
     * @param title window title
     */
    protected BaseFrame(String title) {
        super(title);
        configureWindow();
    }

    /**
     * Applies common frame settings used by all dashboards.
     */
    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------------
    // Hook methods for subclasses
    // ---------------------------------------------------------------------

    /**
     * Allows subclasses to create and arrange their components.  This method
     * is invoked by {@link #init()} and has an empty default implementation.
     */
    protected void initializeComponents() {
        // no-op default
    }

    /**
     * Allows subclasses to register event listeners.  This method is invoked
     * by {@link #init()} and has an empty default implementation.
     */
    protected void registerListeners() {
        // no-op default
    }

    /**
     * Template method that invokes the lifecycle hook methods.  Subclasses
     * should call this at the end of their constructors once their fields are
     * initialized.
     */
    protected final void init() {
        initializeComponents();
        registerListeners();
    }
}

