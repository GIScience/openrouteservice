package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.defaults.DefaultEngineProperties;
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
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode
public class EngineProperties {

    @JsonIgnore
    @Getter(AccessLevel.PROTECTED)
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
    @JsonProperty("config_output_mode")
    private Boolean configOutputMode;
    @JsonProperty("graphs_root_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path graphsRootPath;
    @JsonProperty("graphs_data_access")
    private DataAccessEnum graphsDataAccess;

    @JsonProperty("elevation")
    private ElevationProperties elevation;
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
            // Fifth step: Set the graph path correctly for the default profiles
            profile.setGraphPath(Paths.get(this.getGraphsRootPath().toString(), profileEntryName).toAbsolutePath());

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
            // Fifth step: Set the graph path correctly for the user profiles
            if (profile.getGraphPath() == null || profile.getGraphPath().equals(emptyPath)) {
                profile.setGraphPath(Paths.get(this.getGraphsRootPath().toString(), profileEntryName).toAbsolutePath());
            }
        }
        setInitialized(true);
    }

    /**
     * Copy properties from the source to this object.
     * This is a positive update function. It will only update the properties that are not set in this object.
     * If the source contains null values, they will be ignored.
     * If the source contains a value, it will be set in this object.
     * If this object already contains a non-null value, it will conditionally be overwritten based on the overwrite flag.#
     * This description applies to all subsequent copyProperties functions.
     *
     * @param source    EngineProperties
     * @param overwrite boolean
     */
    @JsonIgnore
    public void copyProperties(EngineProperties source, boolean overwrite) {
        if (source == null) {
            return;
        }

        this.setInitialized(source.isInitialized());

        if (this.getSourceFile() == null) {
            this.setSourceFile(source.getSourceFile());
        } else {
            if (source.getSourceFile() != null && overwrite) {
                this.setSourceFile(source.getSourceFile());
            }
        }

        if (this.getInitThreads() == null) {
            this.setInitThreads(source.getInitThreads());
        } else {
            if (source.getInitThreads() != null && overwrite) {
                this.setInitThreads(source.getInitThreads());
            }
        }

        if (this.getPreparationMode() == null) {
            this.setPreparationMode(source.getPreparationMode());
        } else {
            if (source.getPreparationMode() != null && overwrite) {
                this.setPreparationMode(source.getPreparationMode());
            }
        }

        if (this.getConfigOutputMode() == null) {
            this.setConfigOutputMode(source.getConfigOutputMode());
        } else {
            if (source.getConfigOutputMode() != null && overwrite) {
                this.setConfigOutputMode(source.getConfigOutputMode());
            }
        }

        if (this.getGraphsRootPath() == null) {
            this.setGraphsRootPath(source.getGraphsRootPath());
        } else {
            if (source.getGraphsRootPath() != null && overwrite) {
                this.setGraphsRootPath(source.getGraphsRootPath());
            }
        }

        if (this.getGraphsDataAccess() == null) {
            this.setGraphsDataAccess(source.getGraphsDataAccess());
        } else {
            if (source.getGraphsDataAccess() != null && overwrite) {
                this.setGraphsDataAccess(source.getGraphsDataAccess());
            }
        }

        if (this.getElevation() == null) {
            this.setElevation(source.getElevation());
        } else {
            if (source.getElevation() != null) {
                this.getElevation().copyProperties(source.getElevation(), overwrite);
            }
        }

        if (this.getProfileDefault() == null) {
            this.setProfileDefault(source.getProfileDefault());
        } else {
            if (source.getProfileDefault() != null) {
                this.getProfileDefault().copyProperties(source.profileDefault, overwrite);
            }
        }

        if (this.getProfiles() == null) {
            this.setProfiles(source.getProfiles());
        } else {
            if (source.getProfiles() != null) {
                for (String profileEntryName : source.getProfiles().keySet()) {
                    ProfileProperties sourceProfileProperties = source.getProfiles().get(profileEntryName);
                    ProfileProperties targetProfileProperties = this.getProfiles().get(profileEntryName);
                    if (sourceProfileProperties == null) {
                        continue;
                    }
                    if (targetProfileProperties != null) {
                        this.getProfiles().get(profileEntryName).copyProperties(source.getProfiles().get(profileEntryName), overwrite);
                    } else {
                        this.getProfiles().put(profileEntryName, source.getProfiles().get(profileEntryName));
                    }
                }
            }
        }
    }


    @JsonIgnore
    public Map<String, ProfileProperties> getActiveProfiles() {
        if (!initialized) {
            initialize();
        }
        return profiles;
    }
}
