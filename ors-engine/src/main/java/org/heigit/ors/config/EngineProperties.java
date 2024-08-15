package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.defaults.DefaultEngineProperties;
import org.heigit.ors.config.defaults.DefaultGraphManagementProperties;
import org.heigit.ors.config.defaults.DefaultProfileProperties;
import org.heigit.ors.config.defaults.DefaultProfiles;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.heigit.ors.config.utils.PropertyUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
public class EngineProperties {

    @JsonIgnore
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private boolean initialized = false;

    @JsonProperty("source_file")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path sourceFile;
    @JsonProperty("init_threads")
    private Integer initThreads;
    @JsonProperty("preparation_mode")
    private Boolean preparationMode;
    @JsonProperty("config_output")
    @Setter(AccessLevel.PUBLIC)
    private String configOutput;
    @JsonProperty("graphs_root_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path graphsRootPath;
    @JsonProperty("graphs_data_access")
    private DataAccessEnum graphsDataAccess;

    @JsonProperty("elevation")
    private ElevationProperties elevation;
    @JsonProperty("graph_management")
    private GraphManagementProperties graphManagement;
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault;
    @JsonProperty("profiles")
    private Map<String, ProfileProperties> profiles;

    public EngineProperties() {
    }

    @JsonIgnore
    public void initialize() {
        if (isInitialized()) {
            return;
        }
        // Merge default profiles with custom profiles
        // First: Top priority have properties from Map<String, ProfileProperties> profiles;
        // Second: Next priority are user set global properties from profileDefault
        // Third: If properties are not set in profiles and profileDefault, use the default_profiles with their specific properties and their defaults
        // Fourth: If properties are not set in profiles, profileDefault and default_profiles, use the default properties from DefaultProfileProperties

        // Initialize default engine properties
        EngineProperties default_engine_properties = new DefaultEngineProperties(true);

        // Set base graph path for the default engine properties if set by the user
        Path emptyPath = Path.of("");
        if (this.getGraphsRootPath() == null || this.getGraphsRootPath().equals(emptyPath)) {
            this.setGraphsRootPath(default_engine_properties.getGraphsRootPath());
        }

        // Correct the default profiles that haven't been set by the user
        HashSet<String> raw_user_profile_names;
        if (this.getProfiles() == null) {
            // In case the user has not set any profiles, we initialize the profiles with an empty map.
            this.setProfiles(new HashMap<>());
            raw_user_profile_names = new HashSet<>();
        } else {
            // Make a copy of the raw user profile names for later
            raw_user_profile_names = new HashSet<>(this.getProfiles().keySet());
        }

        // Get the raw user default profile settings
        ProfileProperties raw_user_default_profile_settings = this.getProfileDefault();
        DefaultProfiles system_default_profile_settings = new DefaultProfiles(true);
        DefaultProfileProperties system_default_profile_defaults_properties = new DefaultProfileProperties(true);

        if (this.getGraphManagement() == null) {
            this.setGraphManagement(new DefaultGraphManagementProperties());
        }

        for (String profileEntryName : system_default_profile_settings.getProfiles().keySet()) {
            ProfileProperties profile = system_default_profile_settings.getProfiles().get(profileEntryName);
            if (this.getProfiles().containsKey(profileEntryName)) {
                continue;
            }
            // Second step
            PropertyUtils.deepCopyObjectsProperties(raw_user_default_profile_settings, profile, true);
            // Third step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_settings.getProfiles().get(profile.getEncoderName().toString()), profile, false);
            // Fourth step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_defaults_properties, profile, false);

            this.profiles.put(profileEntryName, profile);
        }

        // Enrich null or missing properties with default values
        PropertyUtils.deepCopyObjectsProperties(default_engine_properties, this, false);

        // Correct the raw user profiles
        for (String profileEntryName : raw_user_profile_names) {
            // First step
            ProfileProperties profile = this.getProfiles().get(profileEntryName);
            // Second step
            PropertyUtils.deepCopyObjectsProperties(raw_user_default_profile_settings, profile, false);
            // Third step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_settings.getProfiles().get(profileEntryName), profile, false);
            // Fourth step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_defaults_properties, profile, false);
        }
        setInitialized(true);
    }


    @JsonIgnore
    public Map<String, ProfileProperties> getActiveProfiles() {
        if (!initialized) {
            initialize();
        }
        return profiles;
    }
}
