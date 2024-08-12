package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@JsonTypeName("HereTraffic")
@JsonPropertyOrder({"enabled", "streets", "ref_pattern", "pattern", "radius", "output_log", "log_location"})
@EqualsAndHashCode(callSuper = true)
public class ExtendedStorageHereTraffic extends ExtendedStorage {
    Logger logger = LoggerFactory.getLogger(ExtendedStorageHereTraffic.class);


    private Path streets = Path.of("");

    private Path ref_pattern = Path.of("");

    private Path pattern_15min = Path.of("");

    private Integer radius = 150;

    private Boolean output_log = false;

    private Path log_location = Path.of("");

    @Getter
    @Setter
    private String ghProfile;

    public ExtendedStorageHereTraffic() {
    }

    @JsonCreator
    public ExtendedStorageHereTraffic(String ignoredEmpty) {
        logger.warn("HereTraffic storage is not correctly configured and will be disabled.");
        setEnabled(false);
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

    @JsonProperty(value = "pattern_15min")
    @JsonSerialize(using = PathSerializer.class)
    public Path getPattern_15min() {
        return pattern_15min;
    }

    @JsonSetter("pattern_15min")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setPattern_15min(Path pattern_15min) {
        this.pattern_15min = pattern_15min;
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

    @JsonIgnore
    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value instanceof ExtendedStorageHereTraffic storage) {
            Path emptyString = Path.of("");

            if (this.getStreets().equals(emptyString) || (!storage.getStreets().equals(emptyString) && overwrite)) {
                this.setStreets(storage.getStreets());
            }

            if (this.getRefPattern().equals(emptyString) || (!storage.getRefPattern().equals(emptyString) && overwrite)) {
                this.setRefPattern(storage.getRefPattern());
            }

            if (this.getPattern_15min().equals(emptyString) || (!storage.getPattern_15min().equals(emptyString) && overwrite)) {
                this.setPattern_15min(storage.getPattern_15min());
            }

            if (this.getRadius() == null || (storage.getRadius() != null && overwrite)) {
                this.setRadius(storage.getRadius());
            }

            if (this.getOutputLog() == null || (storage.getOutputLog() != null && overwrite)) {
                this.setOutputLog(storage.getOutputLog());
            }

            if (this.getLogLocation().equals(emptyString) || (!storage.getLogLocation().equals(emptyString) && overwrite)) {
                this.setLogLocation(storage.getLogLocation());
            }
        }
    }
}
