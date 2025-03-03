package org.heigit.ors.benchmark.util;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class providing iterator-related functionality.
 */
public final class IteratorUtils {

    private IteratorUtils() {
        // Prevent instantiation
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates an infinite iterator that cycles through a list endlessly.
     *
     * @param <T>  the type of elements in the list
     * @param list the list to iterate over
     * @return an iterator that will cycle through the list indefinitely
     * @throws IllegalArgumentException if list is null or empty
     */
    public static <T> Iterator<T> infiniteCircularIterator(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be null or empty");
        }

        int size = list.size();
        return new Iterator<T>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                T value = list.get(i);
                i = (i + 1) % size;
                return value;
            }
        };
    }
}
