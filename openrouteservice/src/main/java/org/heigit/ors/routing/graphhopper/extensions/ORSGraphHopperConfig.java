package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.util.ArrayList;
import java.util.List;

public class ORSGraphHopperConfig extends GraphHopperConfig {
    private List<CHProfile> coreProfiles = new ArrayList<>();
    private List<Profile> fastisochroneProfiles = new ArrayList<>();

    public List<CHProfile> getCoreProfiles() {
        return coreProfiles;
    }

    public GraphHopperConfig setCoreProfiles(List<CHProfile> coreProfiles) {
        this.coreProfiles = coreProfiles;
        return this;
    }

    public List<Profile> getFastisochroneProfiles() {
        return fastisochroneProfiles;
    }

    public GraphHopperConfig setFastisochroneProfiles(List<Profile> fastisochroneProfiles) {
        this.fastisochroneProfiles = fastisochroneProfiles;
        return this;
    }
}
