package org.heigit.ors.model;

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
}
