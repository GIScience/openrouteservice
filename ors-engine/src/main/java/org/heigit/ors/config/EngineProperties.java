package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String configOutput;
    private DataAccessEnum graphsDataAccess;

    private ElevationProperties elevation = new ElevationProperties();
    @JsonProperty("graph_management")
    private GraphManagementProperties graphManagement = new GraphManagementProperties();
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault = new ProfileProperties();
    private Map<String, ProfileProperties> profiles = new LinkedHashMap<>();

    @JsonIgnore
    public Map<String, ProfileProperties> getInitializedActiveProfiles() {
        LinkedHashMap<String, ProfileProperties> activeProfiles = new LinkedHashMap<>();
        for (Map.Entry<String, ProfileProperties> entry : profiles.entrySet()) {
            ProfileProperties mergedProfile = entry.getValue().mergeDefaults(profileDefault, entry.getKey());
            if (Boolean.TRUE.equals(mergedProfile.getEnabled())) {
                activeProfiles.put(entry.getKey(), mergedProfile);
            }
        }
        return activeProfiles;
    }
}
