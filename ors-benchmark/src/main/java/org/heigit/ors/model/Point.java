package org.heigit.ors.model;

import java.util.Objects;

/**
 * Represents a snapped coordinate point with support for coordinate comparison
 * with precision tolerance.
 */
public class Point {
    private static final double COORDINATE_PRECISION = 0.000001;
    private final double[] coordinates;
    private final String profile;

    public Point(double[] coordinates, String profile) {
        this.coordinates = coordinates;
        this.profile = profile;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public String getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Point point = (Point) o;
        return coordsEqual(coordinates, point.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Math.round(coordinates[0] * 1e6) / 1e6,
                Math.round(coordinates[1] * 1e6) / 1e6);
    }

    private boolean coordsEqual(double[] coord1, double[] coord2) {
        return Math.abs(coord1[0] - coord2[0]) < COORDINATE_PRECISION &&
                Math.abs(coord1[1] - coord2[1]) < COORDINATE_PRECISION;
    }
}
