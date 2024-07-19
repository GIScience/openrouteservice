package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.heigit.ors.api.converters.PathDeserializer;
import org.heigit.ors.api.converters.PathSerializer;

import java.nio.file.Path;

@JsonTypeName("HereTraffic")
@JsonPropertyOrder({"enabled", "streets", "ref_pattern", "pattern"})
public class ExtendedStorageHereTraffic extends ExtendedStorage {
    private Path streets;

    private Path ref_pattern;

    private Path pattern;

    public ExtendedStorageHereTraffic() {
    }

    @JsonProperty("streets")
    @JsonSerialize(using = PathSerializer.class)
    public Path getStreets() {
        return streets;
    }

    @JsonSetter("streets")
    @JsonDeserialize(using = PathDeserializer.class)
    public void setStreets(Path streets) {
        if (streets != null && !streets.toString().isEmpty())
            this.streets = streets.toAbsolutePath();
    }

    @JsonProperty("ref_pattern")
    @JsonSerialize(using = PathSerializer.class)
    public Path getRefPattern() {
        return ref_pattern;
    }

    @JsonSetter("ref_pattern")
    @JsonDeserialize(using = PathDeserializer.class)
    public void setRefPattern(Path ref_pattern) {
        if (ref_pattern != null && !ref_pattern.toString().isEmpty())
            this.ref_pattern = ref_pattern.toAbsolutePath();
    }

    @JsonProperty("pattern")
    @JsonSerialize(using = PathSerializer.class)
    public Path getPattern() {
        return pattern;
    }

    @JsonSetter("pattern")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setPattern(Path pattern) {
        this.pattern = pattern;
    }
}
