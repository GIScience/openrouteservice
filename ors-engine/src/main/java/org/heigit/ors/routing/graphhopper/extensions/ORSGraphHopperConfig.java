package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;

import java.util.ArrayList;
import java.util.List;

public class ORSGraphHopperConfig extends GraphHopperConfig {
    public static final String KEY_PREPARE_CORELM_THREADS = "prepare.corelm.threads";
    private List<CHProfile> coreProfiles = new ArrayList<>();
    private List<LMProfile> coreLMProfiles = new ArrayList<>();
    private List<Profile> fastisochroneProfiles = new ArrayList<>();

    public List<CHProfile> getCoreProfiles() {
        return coreProfiles;
    }

    public void setCoreProfiles(List<CHProfile> coreProfiles) {
        this.coreProfiles = coreProfiles;
    }

    public List<LMProfile> getCoreLMProfiles() {
        return coreLMProfiles;
    }

    public void setCoreLMProfiles(List<LMProfile> coreLMProfiles) {
        this.coreLMProfiles = coreLMProfiles;
        if (has(KEY_PREPARE_CORELM_THREADS))
            putObject(KEY_PREPARE_CORELM_THREADS, getInt(KEY_PREPARE_CORELM_THREADS, 1));
    }

    public List<Profile> getFastisochroneProfiles() {
        return fastisochroneProfiles;
    }

    public void setFastisochroneProfiles(List<Profile> fastisochroneProfiles) {
        this.fastisochroneProfiles = fastisochroneProfiles;
    }
}
