package org.heigit.ors.coordinates_generator.model;

import java.util.Arrays;
import java.util.Objects;

public class Matrix {
    private final double[][] coordinates;
    private final int[] sources;
    private final int[] destinations;
    private final double[][] distances;
    private final String profile;

    public Matrix(double[][] coordinates, int[] sources, int[] destinations, double[][] distances, String profile) {
        this.coordinates = coordinates;
        this.sources = sources;
        this.destinations = destinations;
        this.distances = distances;
        this.profile = profile;
    }

    public double[][] getCoordinates() {
        return coordinates;
    }

    public int[] getSources() {
        return sources;
    }

    public int[] getDestinations() {
        return destinations;
    }

    public double[][] getDistances() {
        return distances;
    }

    public String getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix matrix = (Matrix) obj;
        return Arrays.deepEquals(coordinates, matrix.coordinates)
                && Arrays.equals(sources, matrix.sources)
                && Arrays.equals(destinations, matrix.destinations)
                && Arrays.deepEquals(distances, matrix.distances)
                && profile.equals(matrix.profile);
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(coordinates);
        result = 31 * result + Arrays.hashCode(sources);
        result = 31 * result + Arrays.hashCode(destinations);
        result = 31 * result + Arrays.deepHashCode(distances);
        result = 31 * result + profile.hashCode();
        return result;
    }
}
