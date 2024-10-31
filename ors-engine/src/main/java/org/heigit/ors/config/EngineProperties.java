package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.profile.ProfileProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class EngineProperties {
    private Integer initThreads;
    private Boolean preparationMode;
    @JsonIgnore
    private String configOutput;
    private DataAccessEnum graphsDataAccess;

    private ElevationProperties elevation = new ElevationProperties();
    private GraphManagementProperties graphManagement = new GraphManagementProperties();
    private ProfileProperties profileDefault = new ProfileProperties();
    private Map<String, ProfileProperties> profiles = new LinkedHashMap<>();

    @JsonIgnore
    public Map<String, ProfileProperties> getInitializedActiveProfiles() {
        LinkedHashMap<String, ProfileProperties> activeProfiles = new LinkedHashMap<>();
        profiles.forEach((key, profile) -> {
            profile.getBuild().initExtStorages();
            ProfileProperties mergedProfile = profile.mergeDefaults(profileDefault, key);
            if (Boolean.TRUE.equals(mergedProfile.getEnabled())) {
                activeProfiles.put(key, mergedProfile);
            }
        });
        return activeProfiles;
    }
}
