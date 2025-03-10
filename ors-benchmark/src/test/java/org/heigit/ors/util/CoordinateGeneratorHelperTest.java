package org.heigit.ors.util;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for the CoordinateGeneratorHelper utility class.
 */
class CoordinateGeneratorHelperTest {

    private static final double[] TEST_EXTENT = new double[] { 8.573179, 49.352003, 8.793986, 49.459693 };
    private static final double DELTA = 1e-10;

    @Test
    void randomCoordinatesInExtent_WithoutConstraints_ReturnsRequestedNumberOfCoordinates() {
        // Arrange
        int count = 10;

        // Act
        List<double[]> coordinates = CoordinateGeneratorHelper.randomCoordinatesInExtent(count, TEST_EXTENT);

        // Assert
        assertEquals(count, coordinates.size());
        for (double[] coord : coordinates) {
            assertCoordinateWithinExtent(coord, TEST_EXTENT);
        }
    }

    @Test
    void calculateHaversineDistance_ReturnsCorrectDistance() {
        // Arrange
        double[] berlin = new double[] { 13.405, 52.52 }; // Berlin
        double[] paris = new double[] { 2.3522, 48.8566 }; // Paris
        double expectedDistance = 878600; // ~878.6 km (straight line)
        double tolerance = 1200; // Allow 1.2 km tolerance

        // Act
        double distance = CoordinateGeneratorHelper.calculateHaversineDistance(berlin, paris);

        // Assert
        assertEquals(expectedDistance, distance, tolerance,
                "Distance between Berlin and Paris should be approximately 878.6 km");
    }
    
    @Test
    void calculateHaversineDistance_SamePoint_ReturnsZero() {
        // Arrange
        double[] point = new double[] { 8.676, 49.418 };
        
        // Act
        double distance = CoordinateGeneratorHelper.calculateHaversineDistance(point, point);
        
        // Assert
        assertEquals(0.0, distance, DELTA, "Distance between same points should be zero");
    }

    /**
     * Test that multiple generated points are distributed across the extent.
     */
    @Test
    void randomCoordinatesInExtent_MultiplePoints_AreDistributed() {
        int numPoints = 100;
        List<double[]> points = CoordinateGeneratorHelper.randomCoordinatesInExtent(numPoints, TEST_EXTENT);

        // Check distribution by dividing the extent into quadrants and verifying points
        // fall in each
        double midLon = (TEST_EXTENT[0] + TEST_EXTENT[2]) / 2;
        double midLat = (TEST_EXTENT[1] + TEST_EXTENT[3]) / 2;

        boolean foundNW = false;
        boolean foundNE = false;
        boolean foundSW = false;
        boolean foundSE = false;

        for (double[] point : points) {
            if (point[0] < midLon && point[1] > midLat)
                foundNW = true;
            if (point[0] >= midLon && point[1] > midLat)
                foundNE = true;
            if (point[0] < midLon && point[1] <= midLat)
                foundSW = true;
            if (point[0] >= midLon && point[1] <= midLat)
                foundSE = true;

            // If all quadrants have been hit, we can stop checking
            if (foundNW && foundNE && foundSW && foundSE)
                break;
        }

        assertTrue(foundNW, "Should have at least one point in the northwest quadrant");
        assertTrue(foundNE, "Should have at least one point in the northeast quadrant");
        assertTrue(foundSW, "Should have at least one point in the southwest quadrant");
        assertTrue(foundSE, "Should have at least one point in the southeast quadrant");
    }

    @Test
    void testRandomCoordinateInRadius_ValidatesNullCenter() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CoordinateGeneratorHelper.randomCoordinateInRadius(null, 1000);
        });
        assertEquals("Center point must be a double array with 2 values [lon, lat]", exception.getMessage());
    }

    @Test
    void testRandomCoordinateInRadius_ValidatesInvalidCenterSize() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CoordinateGeneratorHelper.randomCoordinateInRadius(new double[] { 1.0 }, 1000);
        });
        assertEquals("Center point must be a double array with 2 values [lon, lat]", exception.getMessage());
    }

    @Test
    void testRandomCoordinateInRadius_ValidatesNegativeRadius() {
        double[] center = { 13.4, 52.5 }; // Berlin
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CoordinateGeneratorHelper.randomCoordinateInRadius(center, -100);
        });
        assertEquals("Radius must be greater than 0", exception.getMessage());
    }

    @Test
    void testRandomCoordinateInRadius_ValidatesZeroRadius() {
        double[] center = { 8.6881, 49.4054 }; // Heidelberg
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CoordinateGeneratorHelper.randomCoordinateInRadius(center, 0);
        });
        assertEquals("Radius must be greater than 0", exception.getMessage());
    }

    @RepeatedTest(100) // Run this test multiple times to verify randomness
    void testRandomCoordinateInRadius_GeneratesPointAtCorrectDistance() {
        double[] center = { 8.6881, 49.4054 }; // Heidelberg
        double radius = 10000; // 10 km

        double[] result = CoordinateGeneratorHelper.randomCoordinateInRadius(center, radius);

        // Verify result format
        assertEquals(2, result.length);

        // Calculate distance between center and generated point
        double distance = CoordinateGeneratorHelper.calculateHaversineDistance(center, result);

        assertTrue(distance <= radius, "Distance should be less than or equal to the radius");
    }

    @ParameterizedTest
    @ValueSource(doubles = { 2.0, 100.0, 1000.0, 10000.0, 50000.0 })
    void testRandomCoordinateInRadius_WorksWithDifferentRadii(double radius) {
        double[] center = { 0.0, 0.0 }; // Null Island

        double[] result = CoordinateGeneratorHelper.randomCoordinateInRadius(center, radius);

        // Calculate distance between center and generated point
        double distance = CoordinateGeneratorHelper.calculateHaversineDistance(center, result);

        // Distance should be very close to the specified radius
        assertTrue(distance <= radius, "Distance should be less than or equal to the radius");
    }

    @RepeatedTest(10)
    void testRandomCoordinateInRadius_HandlesEdgeCases() {
        // Test near poles
        double[] northPole = { 0.0, 89.9 }; // Near North Pole
        double[] result = CoordinateGeneratorHelper.randomCoordinateInRadius(northPole, 10000);
        double distance = CoordinateGeneratorHelper.calculateHaversineDistance(northPole, result);
        assertTrue(distance <= 10000, "Distance should be less than or equal to the radius");

        // Test near the anti meridian (180°/-180° longitude)
        double[] antiMeridian = { 179.9, 0.0 };
        result = CoordinateGeneratorHelper.randomCoordinateInRadius(antiMeridian, 10000);
        distance = CoordinateGeneratorHelper.calculateHaversineDistance(antiMeridian, result);
        assertTrue(distance <= 10000, "Distance should be less than or equal to the radius");
    }

    @RepeatedTest(10)
     void testRandomCoordinateInRadius_GeneratesDifferentPoints() {
        double[] center = { 8.6881, 49.4054 }; // Heidelberg
        double radius = 5000; // 5 km

        // Generate two points and verify they're different
        double[] point1 = CoordinateGeneratorHelper.randomCoordinateInRadius(center, radius);
        double[] point2 = CoordinateGeneratorHelper.randomCoordinateInRadius(center, radius);

        // Points should be different (extremely low probability they'd be identical)
        boolean pointsAreDifferent = point1[0] != point2[0] || point1[1] != point2[1];
        assertTrue(pointsAreDifferent, "Generated points should be different");

        // Distance between points should be within the radius
        double distance = CoordinateGeneratorHelper.calculateHaversineDistance(point1, point2);
        assertTrue(distance <= radius * 2, "Distance between points should be within the radius");
    }

    @Test
    void testRandomCoordinateInRadius_OutputFormat() {
        double[] center = { 8.6881, 49.4054 }; // Heidelberg
        double radius = 1000; // 1 km

        double[] result = CoordinateGeneratorHelper.randomCoordinateInRadius(center, radius);

        // Verify we get longitude, latitude in that order
        // For a relatively small radius, the result should still be close to the center
        assertTrue(Math.abs(result[0] - center[0]) < 1.0, "Longitude should be within 1 degree");
        assertTrue(Math.abs(result[1] - center[1]) < 1.0, "Latitude should be within 1 degree");
    }
    private void assertCoordinateWithinExtent(double[] coordinate, double[] extent, String message) {
        assertTrue(coordinate[0] >= extent[0] && coordinate[0] <= extent[2],
                message != null ? message : "Longitude " + coordinate[0] + " should be within [" + extent[0] + ", " + extent[2] + "]");
        assertTrue(coordinate[1] >= extent[1] && coordinate[1] <= extent[3],
                message != null ? message : "Latitude " + coordinate[1] + " should be within [" + extent[1] + ", " + extent[3] + "]");
    }

    private void assertCoordinateWithinExtent(double[] coordinate, double[] extent) {
        assertCoordinateWithinExtent(coordinate, extent, null);
    }

    @RepeatedTest(5)
    void testRandomCoordinateInRadiusAndExtent_MultipleCalls() {
        double[] center = {8.6821, 49.4141};
        double radiusMeters = 2000;
        double[] extent = {8.65, 49.39, 8.71, 49.44}; // Restricted area around Heidelberg
        
        // Generate multiple points
        double[] point1 = CoordinateGeneratorHelper.randomCoordinateInRadiusAndExtent(center, radiusMeters, extent);
        double[] point2 = CoordinateGeneratorHelper.randomCoordinateInRadiusAndExtent(center, radiusMeters, extent);
        
        // Verify both points are valid
        assertNotNull(point1);
        assertNotNull(point2);
        assertEquals(2, point1.length);
        assertEquals(2, point2.length);
        
        // Check that both points are within the radius
        double distance1 = CoordinateGeneratorHelper.calculateHaversineDistance(center, point1);
        double distance2 = CoordinateGeneratorHelper.calculateHaversineDistance(center, point2);
        assertTrue(distance1 <= radiusMeters, "First point should be within the specified radius");
        assertTrue(distance2 <= radiusMeters, "Second point should be within the specified radius");
        
        // Check that both points are within the extent
        assertCoordinateWithinExtent(point1, extent, "First point should be within extent");
        assertCoordinateWithinExtent(point2, extent, "Second point should be within extent");
        
        // Points should be different (very low probability they'd be identical)
        boolean pointsAreDifferent = point1[0] != point2[0] || point1[1] != point2[1];
        assertTrue(pointsAreDifferent, "Generated points should be different");
    }
}