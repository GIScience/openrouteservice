package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.heigit.ors.api.converters.PathDeserializer;
import org.heigit.ors.api.converters.PathSerializer;

import java.nio.file.Path;

@JsonTypeName("HereTraffic")
@JsonPropertyOrder({"enabled", "streets", "ref_pattern", "pattern", "radius", "output_log", "log_location"})
public class ExtendedStorageHereTraffic extends ExtendedStorage {

    private Path streets = Path.of("");

    private Path ref_pattern = Path.of("");

    private Path pattern = Path.of("");

    private Integer radius = 250;

    private Boolean output_log = false;

    private Path log_location = Path.of("");

    public ExtendedStorageHereTraffic() {
    }

    @JsonProperty(value = "streets")
    @JsonSerialize(using = PathSerializer.class)
    public Path getStreets() {
        return streets;
    }

    @JsonSetter("streets")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setStreets(Path streets) {
        if (streets != null && !streets.toString().isEmpty()) this.streets = streets.toAbsolutePath();
    }

    @JsonProperty(value = "ref_pattern")
    @JsonSerialize(using = PathSerializer.class)
    public Path getRefPattern() {
        return ref_pattern;
    }

    @JsonSetter("ref_pattern")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setRefPattern(Path ref_pattern) {
        if (ref_pattern != null && !ref_pattern.toString().isEmpty()) this.ref_pattern = ref_pattern.toAbsolutePath();
    }

    @JsonProperty(value = "pattern")
    @JsonSerialize(using = PathSerializer.class)
    public Path getPattern() {
        return pattern;
    }

    @JsonSetter("pattern")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setPattern(Path pattern) {
        this.pattern = pattern;
    }

    @JsonProperty(value = "radius")
    public Integer getRadius() {
        return radius;
    }

    @JsonSetter("radius")
    private void setRadius(Integer radius) {
        this.radius = radius;
    }

    @JsonProperty(value = "output_log")
    public Boolean getOutputLog() {
        return output_log;
    }

    @JsonSetter("output_log")
    private void setOutputLog(Boolean output_log) {
        this.output_log = output_log;
    }

    @JsonProperty(value = "log_location")
    @JsonSerialize(using = PathSerializer.class)
    public Path getLogLocation() {
        return log_location;
    }

    @JsonSetter("log_location")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setLogLocation(Path log_location) {
        if (log_location != null && !log_location.toString().isEmpty())
            this.log_location = log_location.toAbsolutePath();
    }
}
