package org.heigit.ors.benchmark.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IteratorUtilsTest {

    @Test
    void testInfiniteCircularIteratorWithValidList() {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        Iterator<Integer> iterator = IteratorUtils.infiniteCircularIterator(numbers);

        // Test multiple cycles
        for (int cycle = 0; cycle < 3; cycle++) {
            assertEquals(1, iterator.next());
            assertEquals(2, iterator.next());
            assertEquals(3, iterator.next());
        }
    }

    @Test
    void testInfiniteCircularIteratorWithSingleElement() {
        List<String> singleElement = List.of("test");
        Iterator<String> iterator = IteratorUtils.infiniteCircularIterator(singleElement);

        // Should keep returning the same element
        for (int i = 0; i < 5; i++) {
            assertEquals("test", iterator.next());
        }
    }

    @Test
    void testInfiniteCircularIteratorAlwaysHasNext() {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        Iterator<Integer> iterator = IteratorUtils.infiniteCircularIterator(numbers);

        // Should always have next element
        for (int i = 0; i < 100; i++) {
            assertTrue(iterator.hasNext());
            iterator.next();
        }
    }

    @Test
    void testInfiniteCircularIteratorWithNullList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> IteratorUtils.infiniteCircularIterator(null));
        assertEquals("List cannot be null or empty", exception.getMessage());
    }

    @Test
    void testInfiniteCircularIteratorWithEmptyList() {
        ArrayList<Object> arrayList = new ArrayList<>();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> IteratorUtils.infiniteCircularIterator(arrayList));
        assertEquals("List cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 5, 10, 100 })
    void testInfiniteCircularIteratorForNIterations(int iterations) {
        List<String> elements = Arrays.asList("a", "b", "c");
        Iterator<String> iterator = IteratorUtils.infiniteCircularIterator(elements);
        int elementCount = elements.size();

        for (int i = 0; i < iterations * elementCount; i++) {
            String expected = elements.get(i % elementCount);
            assertEquals(expected, iterator.next(),
                    String.format("Failed at iteration %d", i));
        }
    }

    @Test
    void testInfiniteCircularIteratorWithDifferentTypes() {
        // Test with Strings
        List<String> strings = Arrays.asList("a", "b", "c");
        Iterator<String> stringIterator = IteratorUtils.infiniteCircularIterator(strings);
        assertEquals("a", stringIterator.next());
        assertEquals("b", stringIterator.next());

        // Test with Integers
        List<Integer> integers = Arrays.asList(1, 2, 3);
        Iterator<Integer> intIterator = IteratorUtils.infiniteCircularIterator(integers);
        assertEquals(1, intIterator.next());
        assertEquals(2, intIterator.next());

        // Test with custom objects
        record TestObject(String value) {
        }
        List<TestObject> objects = Arrays.asList(
                new TestObject("first"),
                new TestObject("second"));
        Iterator<TestObject> objectIterator = IteratorUtils.infiniteCircularIterator(objects);
        assertEquals("first", objectIterator.next().value());
        assertEquals("second", objectIterator.next().value());
    }

    @Test
    void testInfiniteCircularIteratorConcurrently() throws InterruptedException {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        Iterator<Integer> iterator = IteratorUtils.infiniteCircularIterator(numbers);
        List<Thread> threads = new ArrayList<>();
        List<AssertionError> errors = new ArrayList<>();

        // Create multiple threads that use the iterator
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        int value = iterator.next();
                        assertTrue(value >= 1 && value <= 3);
                    }
                } catch (AssertionError e) {
                    synchronized (errors) {
                        errors.add(e);
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        assertTrue(errors.isEmpty(),
                "Concurrent iteration failed with " + errors.size() + " errors");
    }
}
