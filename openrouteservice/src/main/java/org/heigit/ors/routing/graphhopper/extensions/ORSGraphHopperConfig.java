package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;

import java.util.ArrayList;
import java.util.List;

public class ORSGraphHopperConfig extends GraphHopperConfig {
    private List<CHProfile> coreProfiles = new ArrayList<>();

    public List<CHProfile> getCoreProfiles() {
        return coreProfiles;
    }

    public GraphHopperConfig setCoreProfiles(List<CHProfile> coreProfiles) {
        this.coreProfiles = coreProfiles;
        return this;
    }
}
