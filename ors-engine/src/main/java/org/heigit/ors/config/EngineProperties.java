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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode
public class EngineProperties {

    @JsonIgnore
    private DefaultProfiles default_profiles = new DefaultProfiles(true);

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
        setDefault_profiles(new DefaultProfiles(setDefaults));
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

    public Map<String, ProfileProperties> getDefault_profiles() {
        return default_profiles.getProfiles();
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
