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
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultElevationProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfileProperties;
import org.heigit.ors.config.profile.defaults.DefaultProfiles;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.heigit.ors.config.utils.PropertyUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Getter
@Setter(AccessLevel.PROTECTED)
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
        this(false);
    }

    public EngineProperties(Boolean setDefaults) {
        setProfiles(new LinkedHashMap<>());
        setProfileDefault(new DefaultProfileProperties(setDefaults));
        setElevation(new DefaultElevationProperties(setDefaults));
        if (setDefaults) {
            setSourceFile(Paths.get(""));
            setInitThreads(1);
            setPreparationMode(false);
            setConfigOutputMode(false);
            setGraphsRootPath(Paths.get("./graphs"));
            setGraphsDataAccess(DataAccessEnum.RAM_STORE);
        }
    }

    @JsonIgnore
    public void combineProperties() {
        if (isInitialized()) {
            return;
        }
        // Merge default profiles with custom profiles
        // First: Top priority have properties from Map<String, ProfileProperties> profiles;
        // Second: Next priority are user set global properties from profileDefault
        // Third: If properties are not set in profiles and profileDefault, use the default_profiles with their specific properties and their defaults
        // Fourth: If properties are not set in profiles, profileDefault and default_profiles, use the default properties from DefaultProfileProperties
        // Initialize defult profiles

        // Correct the default profiles that haven't been set by the user
        // Make a copy Set<String>
        Set<String> raw_user_profile_names = new HashSet<>(this.getProfiles().keySet());
        ProfileProperties raw_user_default_profile_settings = this.getProfileDefault();
        DefaultProfiles system_default_profile_settings = new DefaultProfiles(true);
        DefaultProfileProperties system_default_profile_defaults_properties = new DefaultProfileProperties(true);

        for (String profileEntry : system_default_profile_settings.getProfiles().keySet()) {
            ProfileProperties profile = system_default_profile_settings.getProfiles().get(profileEntry);
            if (this.getProfiles().containsKey(profileEntry)) {
                // Todo Still needed or just overwrite the defaults in the end?
                continue;
            }
            // Second step
            PropertyUtils.deepCopyObjectsProperties(raw_user_default_profile_settings, profile, true, false);
            // Third step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_settings.getProfiles().get(profile.getEncoderName().name), profile, false, false);
            // Fourth step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_defaults_properties, profile, false, false);
            this.profiles.put(profileEntry, profile);
        }

        EngineProperties default_engine_properties = new EngineProperties(true);

        // Enrich null or missing properties with default values
        PropertyUtils.deepCopyObjectsProperties(default_engine_properties, this, false, false);

        // Correct the raw user profiles
        for (String profileEntryName : raw_user_profile_names) {
            // First step
            ProfileProperties profile = this.getProfiles().get(profileEntryName);
            // Second step
            PropertyUtils.deepCopyObjectsProperties(raw_user_default_profile_settings, profile, false, false);
            // Third step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_settings.getProfiles().get(profileEntryName), profile, false, false);
            // Fourth step
            PropertyUtils.deepCopyObjectsProperties(system_default_profile_defaults_properties, profile, false, false);
        }
        setInitialized(true);
    }


    @JsonIgnore
    public Map<String, ProfileProperties> getActiveProfiles() {
        Map<String, ProfileProperties> activeProfiles = new LinkedHashMap<>();
        for (Map.Entry<String, ProfileProperties> prof : profiles.entrySet()) {
            prof.getValue().mergeDefaultsAndSetGraphPath(profileDefault, graphsRootPath, prof.getKey());
            if (Boolean.TRUE.equals(prof.getValue().getEnabled()) && prof.getValue().getEncoderName() != null) {
                activeProfiles.put(prof.getKey(), prof.getValue());
            }
        }
        return activeProfiles;
    }
}
