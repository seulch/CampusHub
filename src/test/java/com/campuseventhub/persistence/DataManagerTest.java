package com.campuseventhub.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for DataManager
 */
public class DataManagerTest {
    
    private static final String TEST_FILENAME = "test_data.ser";
    
    // Test data class
    static class TestData implements Serializable {
        private String name;
        private int value;
        
        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return value == testData.value && name.equals(testData.name);
        }
        
        @Override
        public int hashCode() {
            return name.hashCode() + value;
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Clean up any existing test files
        cleanup();
    }
    
    @AfterEach
    public void cleanup() {
        // Clean up test files
        DataManager.deleteDataFile(TEST_FILENAME);
    }
    
    @Test
    public void testSaveAndLoadData() throws IOException, ClassNotFoundException {
        TestData testData = new TestData("Test Object", 42);
        
        // Save data
        DataManager.saveData(TEST_FILENAME, testData);
        
        // Verify file exists
        assertTrue(DataManager.dataFileExists(TEST_FILENAME));
        
        // Load data
        Object loadedData = DataManager.loadData(TEST_FILENAME);
        
        // Verify loaded data
        assertNotNull(loadedData);
        assertTrue(loadedData instanceof TestData);
        assertEquals(testData, (TestData) loadedData);
    }
    
    @Test
    public void testLoadNonExistentFile() throws IOException, ClassNotFoundException {
        Object result = DataManager.loadData("nonexistent.ser");
        assertNull(result);
    }
    
    @Test
    public void testSaveNullData() {
        assertThrows(IllegalArgumentException.class, () -> {
            DataManager.saveData(TEST_FILENAME, null);
        });
    }
    
    @Test
    public void testSaveNullFilename() {
        assertThrows(IllegalArgumentException.class, () -> {
            DataManager.saveData(null, new TestData("test", 1));
        });
    }
    
    @Test
    public void testLoadNullFilename() {
        assertThrows(IllegalArgumentException.class, () -> {
            DataManager.loadData(null);
        });
    }
    
    @Test
    public void testDataFileExists() throws IOException {
        assertFalse(DataManager.dataFileExists(TEST_FILENAME));
        
        // Create a test file
        DataManager.saveData(TEST_FILENAME, new TestData("test", 1));
        
        assertTrue(DataManager.dataFileExists(TEST_FILENAME));
    }
    
    @Test
    public void testDeleteDataFile() throws IOException {
        // Create a test file
        DataManager.saveData(TEST_FILENAME, new TestData("test", 1));
        assertTrue(DataManager.dataFileExists(TEST_FILENAME));
        
        // Delete the file
        boolean deleted = DataManager.deleteDataFile(TEST_FILENAME);
        assertTrue(deleted);
        assertFalse(DataManager.dataFileExists(TEST_FILENAME));
        
        // Try to delete non-existent file
        boolean deletedAgain = DataManager.deleteDataFile(TEST_FILENAME);
        assertFalse(deletedAgain);
    }
    
    @Test
    public void testGetDataFilePath() {
        Path path = DataManager.getDataFilePath(TEST_FILENAME);
        assertNotNull(path);
        assertTrue(path.toString().contains(TEST_FILENAME));
    }
    
    @Test
    public void testCreateBackup() throws IOException {
        // Create some test data
        DataManager.saveData(TEST_FILENAME, new TestData("backup test", 100));
        
        // Create backup - this should not throw an exception
        assertDoesNotThrow(() -> DataManager.createBackup());
    }
    
    @Test
    public void testRestoreFromBackupWithNonExistentFile() {
        boolean result = DataManager.restoreFromBackup("nonexistent_backup.ser");
        assertFalse(result);
    }
}