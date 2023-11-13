package org.heigit.ors.snapping;

import org.heigit.ors.common.ServiceRequest;
import org.locationtech.jts.geom.Coordinate;

public class SnappingRequest extends ServiceRequest {
    private final int profileType;
    private final Coordinate[] locations;
    private final double maximumSearchRadius;

    public SnappingRequest(int profileType, Coordinate[] locations, double maximumSearchRadius) {
        this.profileType = profileType;
        this.locations = locations;
        this.maximumSearchRadius = maximumSearchRadius;
    }

    public int getProfileType() {
        return profileType;
    }

    public Coordinate[] getLocations() {
        return locations;
    }

    public double getMaximumSearchRadius() {
        return maximumSearchRadius;
    }
}
