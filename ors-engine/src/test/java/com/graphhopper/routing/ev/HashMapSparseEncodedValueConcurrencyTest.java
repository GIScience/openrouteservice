package com.graphhopper.routing.ev;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HashMapSparseEncodedValue Concurrency Tests")
class HashMapSparseEncodedValueConcurrencyTest {

    @Test
    @DisplayName("Given multiple threads writing to SparseEncodedValue when concurrent updates occur then no ConcurrentModificationException is thrown")
    void testConcurrentWritesDoNotThrowConcurrentModificationException() throws InterruptedException {
        // Arrange
        HashMapSparseEncodedValue<String> sev = new HashMapSparseEncodedValue<>("test_dataset");
        int numThreads = 10;
        int edgesPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // Act
        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int finalThreadId = threadId;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    for (int i = 0; i < edgesPerThread; i++) {
                        int edgeId = finalThreadId * edgesPerThread + i;
                        sev.set(edgeId, "value_" + edgeId);
                    }
                } catch (ConcurrentModificationException e) {
                    exceptionCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Signal all threads to start
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);

        // Assert
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        assertTrue(completed, "Concurrent update operation did not complete within timeout");
        assertEquals(0, exceptionCount.get(), "ConcurrentModificationException was thrown during concurrent writes");
        assertEquals(numThreads * edgesPerThread, sev.getCount(), "All edge values should be stored");
    }

    @Test
    @DisplayName("Given concurrent reads and writes when updates and reads occur simultaneously then values are consistent")
    void testConcurrentReadsAndWritesAreConsistent() throws InterruptedException {
        // Arrange
        HashMapSparseEncodedValue<Integer> sev = new HashMapSparseEncodedValue<>("test_consistency");
        int numWriterThreads = 5;
        int numReaderThreads = 5;
        int totalOperations = 10000;
        ExecutorService executor = Executors.newFixedThreadPool(numWriterThreads + numReaderThreads);
        CountDownLatch endLatch = new CountDownLatch(numWriterThreads + numReaderThreads);
        AtomicInteger readErrors = new AtomicInteger(0);

        // Act
        // Writer threads
        for (int w = 0; w < numWriterThreads; w++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < totalOperations; i++) {
                        sev.set(i, i);
                    }
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Reader threads
        for (int r = 0; r < numReaderThreads; r++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < totalOperations; i++) {
                        Integer val = sev.get(i);
                        if (val != null && !val.equals(i)) {
                            readErrors.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    readErrors.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertTrue(completed, "Concurrent read/write operation did not complete within timeout");
        assertEquals(0, readErrors.get(), "Read errors occurred during concurrent access");
    }

    @Test
    @DisplayName("Given high concurrency stress when thousands of concurrent operations occur then no data is lost")
    void testHighConcurrencyStress() throws InterruptedException {
        // Arrange
        HashMapSparseEncodedValue<String> sev = new HashMapSparseEncodedValue<>("stress_test");
        int numThreads = 50;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        // Act
        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int finalThreadId = threadId;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        int edgeId = finalThreadId * operationsPerThread + i;
                        sev.set(edgeId, "stress_" + edgeId);
                        if (i % 10 == 0) {
                            // Interleave some read operations
                            sev.get(edgeId);
                        }
                    }
                } finally {
                    endLatch.countDown();
                }
            });
        }

        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertTrue(completed, "Stress test did not complete within timeout");
        assertEquals(numThreads * operationsPerThread, sev.getCount(), "Expected all values to be stored under stress");
    }
}
