package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;

import java.nio.file.Path;
import java.util.ArrayList;

import static java.util.Optional.ofNullable;

@Getter
@Setter
public class ProfileProperties {
    @JsonIgnore
    private String profileName;
    @JsonIgnore
    private Boolean enabled;
    @JsonIgnore
    private Path graphPath;
    @JsonIgnore
    private String gHGraphLocation;
    @JsonProperty("encoder_name")
    private EncoderNameEnum encoderName;
    @JsonProperty("build")
    private BuildProperties build = new BuildProperties();
    @JsonIgnore
    private RepoProperties repo = new RepoProperties();
    @JsonIgnore
    private ServiceProperties service = new ServiceProperties();

    @JsonIgnore
    public ProfileProperties mergeDefaults(ProfileProperties profileDefault, String key) {
        if (profileDefault == null) return this;
        // set the profile name to the key
        profileName = ofNullable(profileName).orElse(key);
        // set values from profileDefault if they are not set
        enabled = ofNullable(enabled).orElse(profileDefault.enabled);
        graphPath = ofNullable(graphPath).orElse(profileDefault.graphPath);
        // deep merge from profileDefault
        service.merge(profileDefault.service);
        build.merge(profileDefault.build);
        return this;
    }

    @JsonIgnore
    public void mergeLoaded(ProfileProperties loadedProfile) {
        if (loadedProfile == null) return;
        // Copy build
        build = ofNullable(loadedProfile.build).orElse(build);
        if (loadedProfile.getEncoderName().equals(EncoderNameEnum.PUBLIC_TRANSPORT) && build.getGtfsFile() == null) {
            // If trying to load a public transport profile, set the GTFS file path to some arbitrary value to enable the GTFS Storage
            build.setGtfsFile(Path.of("gtfs"));
        }
    }

    @JsonIgnore
    public Integer[] getProfilesTypes() {
        ArrayList<Integer> list = new ArrayList<>();
        // TODO check why this originally tries to split the encoderName. Can we add more than one?
        if (encoderName != null && encoderName != EncoderNameEnum.DEFAULT) {
            list.add(encoderName.getValue());
        }
        return list.toArray(new Integer[0]);
    }
}


