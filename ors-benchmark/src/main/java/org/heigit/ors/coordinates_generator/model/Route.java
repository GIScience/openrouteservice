package org.heigit.ors.coordinates_generator.model;

public class Route {
    private final double[] start;
    private final double[] end;
    private final double distance;
    private final String profile;

    public Route(double[] start, double[] end, double distance, String profile) {
        this.start = start;
        this.end = end;
        this.distance = distance;
        this.profile = profile;
    }

    public double[] getStart() {
        return start;
    }

    public double[] getEnd() {
        return end;
    }

    public double getDistance() {
        return distance;
    }

    public String getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Route route = (Route) obj;
        return Double.compare(route.distance, distance) == 0 && java.util.Arrays.equals(start, route.start)
                && java.util.Arrays.equals(end, route.end) && profile.equals(route.profile);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = java.util.Arrays.hashCode(start);
        result = prime * result + java.util.Arrays.hashCode(end);
        result = prime * result
                + (int) (Double.doubleToLongBits(distance) ^ (Double.doubleToLongBits(distance) >>> 32));
        result = prime * result + profile.hashCode();
        return result;
    }
}
