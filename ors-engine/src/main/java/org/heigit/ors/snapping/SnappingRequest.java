package org.heigit.ors.snapping;

import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.matrix.ResolvedLocation;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.WeightingMethod;
import org.locationtech.jts.geom.Coordinate;

import java.util.stream.Stream;

public class SnappingRequest extends ServiceRequest {
    private String profileName;
    private final int profileType;
    private final Coordinate[] locations;
    private final double maximumSearchRadius;
    private int maximumLocations;

    public SnappingRequest(int profileType, Coordinate[] locations, double maximumSearchRadius) {
        this.profileType = profileType;
        this.locations = locations;
        this.maximumSearchRadius = maximumSearchRadius;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
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

    public void setMaximumLocations(int maximumLocations) {
        this.maximumLocations = maximumLocations;
    }

    public int getMaximumLocations() {
        return maximumLocations;
    }

    public SnappingResult computeResult(RoutingProfile rp) throws Exception {
        Snapper snapper = new Snapper(rp, WeightingMethod.RECOMMENDED);

        ResolvedLocation[] resolvedLocations = Stream.of(getLocations()).map(snapper::resolveLocation).toArray(ResolvedLocation[]::new);

        String graphDate = rp.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.import.date");
        return new SnappingResult(resolvedLocations, graphDate);
    }

}
