package org.heigit.ors.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper utility for generating coordinates for benchmarking.
 */
public class CoordinateGeneratorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateGeneratorHelper.class);
    private static final double EARTH_RADIUS_METERS = 6371000; // Earth's radius in meters

    private static final Random random = new Random();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CoordinateGeneratorHelper() {
        // This constructor is not meant to be called
    }

    /**
     * Generates a random coordinate that is both within a radius from a center
     * point AND within the specified extent.
     *
     * @param center       Center point [lon, lat]
     * @param radiusMeters Maximum radius in meters from the center
     * @param extent       Bounding box as [minLon, minLat, maxLon, maxLat]
     * @return Random coordinate [lon, lat] that satisfies both constraints, or null
     *         if impossible
     */
    public static double[] randomCoordinateInRadiusAndExtent(double[] center, double radiusMeters, double[] extent) {
        if (center == null || center.length != 2) {
            throw new IllegalArgumentException("Center point must be a double array with 2 values [lon, lat]");
        }
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0");
        }
        if (extent == null || extent.length != 4) {
            throw new IllegalArgumentException(
                    "Extent must be a double array with 4 values [minLon, minLat, maxLon, maxLat]");
        }

        // First check if center is within extent, otherwise it's impossible
        if (!isWithinExtent(center, extent)) {
            LOGGER.debug("Center point is outside the specified extent - impossible to find a point");
            return new double[0];
        }

        // Alternative approach: find a point within radius that is also within extent
        double[] candidate = findPointWithinRadiusAndExtent(center, radiusMeters, extent);
        if (candidate.length == 2) {
            return candidate;
        }

        LOGGER.error("Could not find a point that satisfies both constraints");
        // Return empty array if no point was found
        return new double[0];
    }

    private static double[] findPointWithinRadiusAndExtent(double[] center, double radiusMeters, double[] extent) {
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            double[] candidate = randomCoordinateInRadius(center, radiusMeters);
            if (isWithinExtent(candidate, extent)) {
                LOGGER.debug("Generated point using alternative approach after {} attempt(s)", i + 1);
                return candidate;
            }
        }
        return new double[0];
    }

    /**
     * Generates a random coordinate at a random distance from a center point.
     *
     * @param center       Center point [lon, lat]
     * @param radiusMeters Maximum radius in meters
     * @return Random coordinate [lon, lat] at a distance between 1 and radiusMeters
     */
    public static double[] randomCoordinateInRadius(double[] center, double radiusMeters) {
        if (center == null || center.length != 2) {
            throw new IllegalArgumentException("Center point must be a double array with 2 values [lon, lat]");
        }
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0");
        }

        // Generate a random angle in radians (0 to 2π)
        double angle = random.nextDouble() * 2 * Math.PI;

        // Generate a random radius between 1 and radiusMeters
        double randomRadius = 1 + random.nextDouble() * (radiusMeters - 1);

        // Convert from polar to Cartesian coordinates at the random distance
        return calculatePointAtDistanceAndBearing(center, randomRadius, angle);
    }

    /**
     * Calculates a coordinate at a specific distance and bearing from a center
     * point.
     *
     * @param center         Center point [lon, lat]
     * @param distanceMeters Distance in meters
     * @param bearingRadians Bearing in radians (0 = North, π/2 = East, etc.)
     * @return Coordinate [lon, lat] at the specified distance and bearing
     */
    private static double[] calculatePointAtDistanceAndBearing(double[] center, double distanceMeters,
            double bearingRadians) {
        // Convert center point to radians
        double lat1 = Math.toRadians(center[1]);
        double lon1 = Math.toRadians(center[0]);

        // Angular distance in radians
        double angDist = distanceMeters / EARTH_RADIUS_METERS;

        // Calculate new latitude
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angDist) +
                Math.cos(lat1) * Math.sin(angDist) * Math.cos(bearingRadians));

        // Calculate new longitude
        double lon2 = lon1 + Math.atan2(
                Math.sin(bearingRadians) * Math.sin(angDist) * Math.cos(lat1),
                Math.cos(angDist) - Math.sin(lat1) * Math.sin(lat2));

        // Convert back to degrees
        double newLat = Math.toDegrees(lat2);
        double newLon = Math.toDegrees(lon2);

        // Normalize longitude to -180 to +180
        if (newLon > 180) {
            newLon -= 360;
        } else if (newLon < -180) {
            newLon += 360;
        }

        return new double[] { newLon, newLat };
    }

    /**
     * Generates random coordinates within the given extent.
     *
     * @param count  The number of coordinates to generate
     * @param extent The bounding box [minX, minY, maxX, maxY]
     * @return List of coordinates
     */
    public static List<double[]> randomCoordinatesInExtent(int count, double[] extent) {
        LOGGER.debug("Generating {} random coordinates within extent [{}, {}, {}, {}]",
                count, extent[0], extent[1], extent[2], extent[3]);

        List<double[]> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            points.add(generateRandomPoint(extent));
        }
        return points;
    }

    public static double[] generateRandomPoint(double[] extent) {
        int maxTries = 100;
        int tries = 0;
        double[] point;
        do {
            double x = random.nextDouble() * (extent[2] - extent[0]) + extent[0];
            double y = random.nextDouble() * (extent[3] - extent[1]) + extent[1];
            point = new double[] { x, y };
            tries++;
            if (tries >= maxTries) {
                LOGGER.warn("Failed to generate point within extent after {} tries", maxTries);
                return point; // Return last attempted point
            }
        } while (!isWithinExtent(point, extent));
        return point;
    }

    private static boolean isWithinExtent(double[] point, double[] extent) {
        return point[0] >= extent[0] && point[0] <= extent[2] &&
                point[1] >= extent[1] && point[1] <= extent[3];
    }

    /**
     * Calculate the haversine distance between two points in meters.
     * 
     * @param p1 First point [lon, lat]
     * @param p2 Second point [lon, lat]
     * @return Distance in meters
     */
    public static double calculateHaversineDistance(double[] p1, double[] p2) {
        double lat1 = Math.toRadians(p1[1]);
        double lon1 = Math.toRadians(p1[0]);
        double lat2 = Math.toRadians(p2[1]);
        double lon2 = Math.toRadians(p2[0]);

        double latDistance = lat2 - lat1;
        double lonDistance = lon2 - lon1;

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c; // Distance in meters
    }

}
