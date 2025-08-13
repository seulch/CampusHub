package com.campuseventhub.config;

import com.campuseventhub.config.ApplicationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Test class for ApplicationConfig singleton thread safety fix.
 * 
 * Tests the thread safety improvements:
 * - Double-checked locking singleton pattern
 * - Thread-safe instance creation
 * - Concurrent access handling
 */
class ApplicationConfigTest {
    
    @Test
    @DisplayName("Should implement singleton pattern correctly")
    void testSingletonPattern() {
        ApplicationConfig instance1 = ApplicationConfig.getInstance();
        ApplicationConfig instance2 = ApplicationConfig.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should be thread-safe under concurrent access")
    void testThreadSafety() throws Exception {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<ApplicationConfig> instances = Collections.synchronizedSet(new HashSet<>());
        
        // Create multiple futures that all try to get the singleton instance
        Future<?>[] futures = new Future[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            futures[i] = executor.submit(() -> {
                ApplicationConfig instance = ApplicationConfig.getInstance();
                instances.add(instance);
                return instance;
            });
        }
        
        // Wait for all threads to complete
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        // All threads should have gotten the same instance
        assertEquals(1, instances.size(), "All threads should get the same singleton instance");
    }
    
    @Test
    @DisplayName("Should handle rapid concurrent access")
    void testRapidConcurrentAccess() throws Exception {
        int threadCount = 100;
        int accessesPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<ApplicationConfig> allInstances = Collections.synchronizedSet(new HashSet<>());
        
        // Each thread will access the singleton multiple times rapidly
        Future<?>[] futures = new Future[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            futures[i] = executor.submit(() -> {
                for (int j = 0; j < accessesPerThread; j++) {
                    ApplicationConfig instance = ApplicationConfig.getInstance();
                    allInstances.add(instance);
                    
                    // Small delay to increase chance of race conditions
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        // Wait for all threads to complete
        for (Future<?> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        
        // Despite all the concurrent access, there should still be only one instance
        assertEquals(1, allInstances.size(), 
            "Even with rapid concurrent access, should only have one singleton instance");
    }
    
    @Test
    @DisplayName("Should maintain singleton across multiple test methods")
    void testSingletonPersistence() {
        ApplicationConfig instance1 = ApplicationConfig.getInstance();
        assertNotNull(instance1);
        
        // Simulate some operations
        performSomeOperations();
        
        ApplicationConfig instance2 = ApplicationConfig.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test
    @DisplayName("Should handle null check scenarios")
    void testNullHandling() {
        // Get instance multiple times to ensure consistency
        ApplicationConfig instance1 = ApplicationConfig.getInstance();
        ApplicationConfig instance2 = ApplicationConfig.getInstance();
        ApplicationConfig instance3 = ApplicationConfig.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);
        
        assertSame(instance1, instance2);
        assertSame(instance2, instance3);
        assertSame(instance1, instance3);
    }
    
    private void performSomeOperations() {
        // Simulate some work that might affect the singleton
        for (int i = 0; i < 1000; i++) {
            ApplicationConfig.getInstance();
        }
    }
    
    @Test
    @DisplayName("Should work correctly with stress testing")
    void testStressTest() throws Exception {
        int iterations = 1000;
        Set<ApplicationConfig> instances = Collections.synchronizedSet(new HashSet<>());
        
        // Rapid sequential access
        for (int i = 0; i < iterations; i++) {
            instances.add(ApplicationConfig.getInstance());
        }
        
        assertEquals(1, instances.size(), "Sequential access should always return same instance");
        
        // Mixed concurrent and sequential access
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 50; j++) {
                    instances.add(ApplicationConfig.getInstance());
                }
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        assertEquals(1, instances.size(), "Mixed access should always return same instance");
    }
}