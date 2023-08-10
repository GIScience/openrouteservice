package org.heigit.ors.snapping;

import org.heigit.ors.matrix.ResolvedLocation;

public class SnappingResult {
    private final ResolvedLocation[] locations;

    public SnappingResult(ResolvedLocation[] locations) {
        this.locations = locations;
    }

    public ResolvedLocation[] getLocations() {
        return locations;
    }
}
