package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;

import java.nio.file.Paths;

@JsonTypeName("HereTraffic")
@JsonPropertyOrder({"enabled", "streets", "ref_pattern", "pattern"})
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class ExtendedStorageHereTraffic extends ExtendedStorage {
    //                    // Replace streets, ref_pattern pattern_15min and log_location with absolute paths
//                    String hereTrafficPath = storage.getValue().get("streets");
//                    if (StringUtils.isNotBlank(hereTrafficPath)) {
//                        storage.getValue().put("streets", Paths.get(hereTrafficPath).toAbsolutePath().toString());
//                    }
//                    String hereTrafficRefPattern = storage.getValue().get("ref_pattern");
//                    if (StringUtils.isNotBlank(hereTrafficRefPattern)) {
//                        storage.getValue().put("ref_pattern", Paths.get(hereTrafficRefPattern).toAbsolutePath().toString());
//                    }
//                    String hereTrafficPattern15min = storage.getValue().get("pattern_15min");
//                    if (StringUtils.isNotBlank(hereTrafficPattern15min)) {
//                        storage.getValue().put("pattern_15min", Paths.get(hereTrafficPattern15min).toAbsolutePath().toString());
//                    }
//                    String hereTrafficLogLocation = storage.getValue().get("log_location");
//                    if (StringUtils.isNotBlank(hereTrafficLogLocation)) {
//                        storage.getValue().put("log_location", Paths.get(hereTrafficLogLocation).toAbsolutePath().toString());
//                    }
    private String streets;

    private String ref_pattern;

    private String pattern;

    public ExtendedStorageHereTraffic() {
    }

    @JsonProperty("streets")
    public String getStreets() {
        return streets;
    }

    @JsonSetter("streets")
    public void setStreets(String streets) {
        // Replace streets with absolute path if not null or empty
        if (streets != null && !streets.isEmpty()) {
            streets = Paths.get(streets).toAbsolutePath().toString();
        }
        this.streets = streets;
    }

    @JsonProperty("ref_pattern")
    public String getRefPattern() {
        return ref_pattern;
    }

    @JsonSetter("ref_pattern")
    public void setRefPattern(String ref_pattern) {
        // Replace ref_pattern with absolute path if not null or empty
        if (ref_pattern != null && !ref_pattern.isEmpty()) {
            ref_pattern = Paths.get(ref_pattern).toAbsolutePath().toString();
        }
        this.ref_pattern = ref_pattern;
    }

    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    @JsonSetter("pattern")
    public void setPattern(String pattern) {
        // Replace pattern with absolute path if not null or empty
        if (pattern != null && !pattern.isEmpty()) {
            pattern = Paths.get(pattern).toAbsolutePath().toString();
        }
        this.pattern = pattern;
    }
}
