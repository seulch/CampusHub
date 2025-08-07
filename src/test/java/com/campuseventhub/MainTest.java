package com.campuseventhub;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    @Test
    public void testMainRunsWithoutException() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}