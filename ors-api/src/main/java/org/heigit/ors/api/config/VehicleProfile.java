package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CarProfile.class, name = "car"),
        @JsonSubTypes.Type(value = HgvProfile.class, name = "hgv")
})
public abstract class VehicleProfile {
    Boolean elevation = true;
    Integer encoder_flags_size = 8;

    protected VehicleProfile() {
    }

    public Boolean getElevation() {
        return elevation;
    }

    public void setElevation(Boolean elevation) {
        this.elevation = elevation;
    }

    public Integer getEncoder_flags_size() {
        return encoder_flags_size;
    }

    public void setEncoder_flags_size(Integer encoder_flags_size) {
        this.encoder_flags_size = encoder_flags_size;
    }
}


class CarProfile extends VehicleProfile {
    String profile = "driving-car";

    public CarProfile() {
        super();
    }

    public CarProfile(String type, String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}

@JsonIgnoreProperties({ "elevation" })
class HgvProfile extends VehicleProfile {
    String profile = "driving-hgv";

    public HgvProfile() {
        super();
    }

    public HgvProfile(String type, String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}

class VehicleProfiles {
    private List<VehicleProfile> profiles;

    private List<VehicleProfile> createDefaultProfiles() {
        return List.of(new CarProfile(), new HgvProfile());
    }

    public VehicleProfiles() {
        this.profiles = createDefaultProfiles();
    }

    public void setProfiles(List<VehicleProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            this.profiles = createDefaultProfiles();
        }
    }

    public List<VehicleProfile> getProfiles() {
        return profiles;
    }
}