package org.heigit.ors.benchmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateGeneratorTest {
    private CoordinateGenerator generator;
    private double[] extent;

    @BeforeEach
    void setUp() {
        extent = new double[] { 8.6286, 49.3590, 8.7957, 49.4715 };
        generator = new CoordinateGenerator(
                100, extent, 1, 100, 100, 350, "driving-car", null);
    }

    // Set the base url to openrouteservice.org and catch the runtime exception
    @Test
    void testCoordinateGeneratorWithMissingApiKey() {
        assertThrows(RuntimeException.class, () -> {
            new CoordinateGenerator(
                    100, extent, 1, 100, 100, 350, "driving-car", "https://openrouteservice.org");
        });
    }

    static Stream<Arguments> pointSizeProvider() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(5),
                Arguments.of(10),
                Arguments.of(20),
                Arguments.of(50),
                Arguments.of(1000));
    }

    @ParameterizedTest
    @MethodSource("pointSizeProvider")
    void testRandomCoordinatesInExtentWithDifferentSizes(int numPoints) {
        List<double[]> points = generator.randomCoordinatesInExtent(numPoints);

        // Test correct number of points
        assertEquals(numPoints, points.size());

        // Test each point is within extent
        for (double[] point : points) {
            assertEquals(2, point.length);
            assertTrue(point[0] >= extent[0] && point[0] <= extent[2],
                    "X coordinate should be within extent");
            assertTrue(point[1] >= extent[1] && point[1] <= extent[3],
                    "Y coordinate should be within extent");
        }
    }

    @Test
    void testRandomCoordinatesInExtentZeroPoints() {
        List<double[]> points = generator.randomCoordinatesInExtent(0);
        assertTrue(points.isEmpty());
    }

    @Test
    void testRandomCoordinatesInExtentNegativePoints() {
        List<double[]> points = generator.randomCoordinatesInExtent(-1);
        assertTrue(points.isEmpty());
    }
}
