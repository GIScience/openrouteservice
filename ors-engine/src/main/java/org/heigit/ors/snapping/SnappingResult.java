package org.heigit.ors.snapping;

import org.heigit.ors.matrix.ResolvedLocation;

public class SnappingResult {
    private final ResolvedLocation[] locations;
    private String graphDate = "";
    public SnappingResult(ResolvedLocation[] locations, String graphDate) {
        this.locations = locations;
        this.graphDate = graphDate;
    }

    public ResolvedLocation[] getLocations() {
        return locations;
    }

    public String getGraphDate() { return graphDate; }
}
