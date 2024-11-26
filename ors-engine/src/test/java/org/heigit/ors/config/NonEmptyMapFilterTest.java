package org.heigit.ors.config;

import org.heigit.ors.config.utils.NonEmptyMapFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NonEmptyMapFilterTest {

    NonEmptyMapFilter filter = new NonEmptyMapFilter();

    public static Stream<Arguments> params() {
        return Stream.of(
                Arguments.of(null, true, "Null should return true"),
                Arguments.of(0, false, "Non-map value should return false"),
                Arguments.of(Integer.valueOf(0), false, "Non-map object should return false"),
                Arguments.of(new HashMap<>(), true, "Empty map should return true"),
                Arguments.of(Map.of("key", "value"), false, "Non-empty map should return false"),
                Arguments.of(Map.of(), false, "Empty immutable map created by Map.of() should return true but doesn't.")
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    void testEquals(Object testObject, boolean expectedResult, String message) {
        assertEquals(expectedResult, filter.equals(testObject), message);
    }

    @Test
    void testHashCode() {
        NonEmptyMapFilter filter1 = new NonEmptyMapFilter();
        NonEmptyMapFilter filter2 = new NonEmptyMapFilter();

        assertEquals(filter1.hashCode(), filter2.hashCode(), "Hash codes should be equal for instances of the same class");
    }
}
