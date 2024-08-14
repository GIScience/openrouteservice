package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode
public class ExtendedStorage {

    // Relevant for all
    @JsonProperty
    private Boolean enabled;

    // Relevant for most index storages
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path filepath;

    // Relevant for HGV profile
    @JsonProperty
    private Boolean restrictions;

    @JsonProperty
    // Relevant for HereTraffic profile
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path streets;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path ref_pattern;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path pattern_15min;
    @JsonProperty
    private Integer radius;
    @JsonProperty
    private Boolean output_log;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path log_location;

    // Relevant for HillIndex
    @JsonProperty("maximum_slope")
    private Integer maximumSlope;

    // Relevant for borders
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path boundaries;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path ids;
    @JsonProperty
    @JsonSerialize(using = PathSerializer.class)
    @JsonDeserialize(using = PathDeserializer.class)
    private Path openborders;

    // Relevant for RoadAccessRestrictions
    @JsonProperty("use_for_warnings")
    private Boolean use_for_warnings;

    // Relevant for Wheelchair
    @JsonProperty("KerbsOnCrossings")
    private Boolean kerbs_on_crossings;

    @Getter
    @Setter
    private String ghProfile;

    @JsonCreator
    public ExtendedStorage() {
    }

    @JsonCreator
    public ExtendedStorage(String ignoredEmpty) {
    }

    @JsonSetter
    protected void setRestrictions(String restrictions) {
        this.restrictions = Boolean.parseBoolean(restrictions);
    }

    @JsonSetter("boundaries")
    protected void setBoundaries(Path boundaries) {
        this.boundaries = boundaries != null && !boundaries.toString().isEmpty() ? boundaries.toAbsolutePath() : boundaries;
    }

    @JsonSetter("ids")
    @JsonDeserialize(using = PathDeserializer.class)
    protected void setIds(Path ref_pattern) {
        this.ids = ref_pattern != null && !ref_pattern.toString().isEmpty() ? ref_pattern.toAbsolutePath() : ref_pattern;
    }

    @JsonSetter("openborders")
    @JsonDeserialize(using = PathDeserializer.class)
    protected void setOpenborders(Path openborders) {
        this.openborders = openborders != null && !openborders.toString().isEmpty() ? openborders.toAbsolutePath() : openborders;
    }

    @JsonSetter("filepath")
    @JsonDeserialize(using = PathDeserializer.class)
    protected void setFilepath(Path filepath) {
        this.filepath = filepath != null && !filepath.toString().isEmpty() ? filepath.toAbsolutePath() : filepath;
    }

}


