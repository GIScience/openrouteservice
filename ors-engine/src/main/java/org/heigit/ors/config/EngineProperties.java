package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.config.profile.defaults.*;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter(AccessLevel.PACKAGE)
public class EngineProperties {

    private static final Map<String, ProfileProperties> DEFAULT_PROFILES = new LinkedHashMap<>();

    static {
        DEFAULT_PROFILES.put("car", new CarProfileProperties());
        DEFAULT_PROFILES.put("hgv", new HgvProfileProperties());
        DEFAULT_PROFILES.put("bike-regular", new BikeRegularProfileProperties());
        DEFAULT_PROFILES.put("bike-electric", new BikeElectricProfileProperties());
        DEFAULT_PROFILES.put("bike-mountain", new BikeMountainProfileProperties());
        DEFAULT_PROFILES.put("bike-road", new BikeRoadProfileProperties());
        DEFAULT_PROFILES.put("walking", new WalkingProfileProperties());
        DEFAULT_PROFILES.put("hiking", new HikingProfileProperties());
        DEFAULT_PROFILES.put("wheelchair", new WheelchairProfileProperties());
        DEFAULT_PROFILES.put("public-transport", new PublicTransportProfileProperties());
    }

    @JsonProperty("source_file")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path sourceFile = Paths.get("");
    @JsonProperty("init_threads")
    private Integer initThreads = 1;
    @JsonProperty("preparation_mode")
    private Boolean preparationMode = false;
    @JsonProperty("config_output_mode")
    private Boolean configOutputMode = false;
    @JsonProperty("graphs_root_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path graphsRootPath = Path.of("graphs").toAbsolutePath();
    @JsonProperty("graphs_data_access")
    private String graphsDataAccess = "RAM_STORE";

    @JsonProperty("elevation")
    private ElevationProperties elevation = new ElevationProperties();
    @JsonProperty("profile_default")
    private ProfileProperties profileDefault = new DefaultProfileProperties();
    @JsonProperty("profiles")
    private Map<String, ProfileProperties> profiles = DEFAULT_PROFILES;

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