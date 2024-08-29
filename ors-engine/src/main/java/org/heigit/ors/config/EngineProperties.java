package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ExtendedStorage;
import org.heigit.ors.config.profile.ExtendedStorageName;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.heigit.ors.common.EncoderNameEnum.*;

@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EngineProperties {
    @JsonProperty("init_threads")
    private Integer initThreads = 2;
    @JsonProperty("preparation_mode")
    private Boolean preparationMode = false;
    @JsonProperty("config_output")
    private String configOutput;
    @JsonProperty("graphs_root_path")
    @JsonSerialize(using = PathSerializer.class)
    private Path graphsRootPath = Path.of("graphs");
    @JsonProperty("graphs_data_access")
    private DataAccessEnum graphsDataAccess = DataAccessEnum.RAM_STORE;

    @JsonProperty("elevation")
    private ElevationProperties elevation = new ElevationProperties();
    @JsonProperty("graph_management")
    private GraphManagementProperties graphManagement = new GraphManagementProperties();
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault = ProfileProperties.getProfileInstance(EncoderNameEnum.DEFAULT);
    @JsonProperty("profiles")
    private Map<String, ProfileProperties> profiles = new LinkedHashMap<>();

    @JsonIgnore
    private boolean initialized = false;

    @JsonIgnore
    public void initProfilesMap() {
        List<EncoderNameEnum> defaultEncoderNames = List.of(DRIVING_CAR, DRIVING_HGV, CYCLING_REGULAR, CYCLING_ROAD, CYCLING_ELECTRIC, CYCLING_MOUNTAIN, FOOT_WALKING, FOOT_HIKING, WHEELCHAIR, PUBLIC_TRANSPORT);
        for (EncoderNameEnum encoderName : defaultEncoderNames) {
            ProfileProperties defaultProfile = ProfileProperties.getProfileInstance(encoderName);
            defaultProfile.mergeDefaults(profileDefault, true);
            if (profiles.containsKey(encoderName.name)) {
                profiles.get(encoderName.name).mergeDefaults(defaultProfile, false);
            } else {
                profiles.put(encoderName.name, defaultProfile);
            }
        }
        for (Map.Entry<String, ProfileProperties> entry : profiles.entrySet()) {
            ProfileProperties profile = entry.getValue();
            EncoderNameEnum encoderName = profile.getEncoderName();
            ProfileProperties defaultProfile = ProfileProperties.getProfileInstance(encoderName);
            defaultProfile.mergeDefaults(profileDefault, true);
            profile.mergeDefaults(defaultProfile, false);
            profile.getExtStorages().forEach(
                    (key, value) -> {
                        if (value != null) {
                            value.initialize(ExtendedStorageName.getEnum(key));
                        } else {
                            profile.getExtStorages().put(key, new ExtendedStorage(ExtendedStorageName.getEnum(key)));
                        }
                    }
            );
        }
    }

    @JsonIgnore
    public Map<String, ProfileProperties> getProfiles() {
        if (!initialized) {
            initProfilesMap();
            initialized = true;
        }
        return profiles;
    }

    @JsonIgnore
    public Map<String, ProfileProperties> getActiveProfiles() {
        if (!initialized) {
            initProfilesMap();
            initialized = true;
        }

        LinkedHashMap<String, ProfileProperties> activeProfiles = new LinkedHashMap<>();
        for (Map.Entry<String, ProfileProperties> entry : profiles.entrySet()) {
            ProfileProperties mergedProfile = entry.getValue().mergeDefaults(profileDefault, false);
            if (Optional.ofNullable(mergedProfile.getEnabled()).orElse(false)) {
                activeProfiles.put(entry.getKey(), mergedProfile);
            }
        }
        return activeProfiles;
    }
}
