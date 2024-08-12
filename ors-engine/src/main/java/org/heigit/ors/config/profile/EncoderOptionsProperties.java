package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
@EqualsAndHashCode
public class EncoderOptionsProperties {
    @JsonProperty("block_fords")
    private Boolean blockFords = null;
    @JsonProperty("consider_elevation")
    private Boolean considerElevation = null;
    @JsonProperty("turn_costs")
    private Boolean turnCosts = null;
    @JsonProperty("use_acceleration")
    private Boolean useAcceleration = null;
    @JsonProperty("maximum_grade_level")
    private Integer maximumGradeLevel = null; // TODO find default
    @JsonProperty("preferred_speed_factor")
    private Double preferredSpeedFactor = null; // TODO find default
    @JsonProperty("problematic_speed_factor")
    private Double problematicSpeedFactor = null; // TODO find default
    @JsonProperty("conditional_access")
    private Boolean conditionalAccess = null;
    @JsonProperty("conditional_speed")
    private Boolean conditionalSpeed = null; // TODO find default

    public EncoderOptionsProperties() {
    }

    @JsonIgnore
    public boolean isEmpty() {
        return blockFords == null && considerElevation == null && turnCosts == null && useAcceleration == null && maximumGradeLevel == null && preferredSpeedFactor == null && problematicSpeedFactor == null && conditionalAccess == null && conditionalSpeed == null;
    }

    @JsonIgnore
    public String toString() {
        List<String> out = new ArrayList<>();
        if (blockFords != null) {
            out.add("block_fords=" + blockFords);
        }
        if (considerElevation != null) {
            out.add("consider_elevation=" + considerElevation);
        }
        if (turnCosts != null) {
            out.add("turn_costs=" + turnCosts);
        }
        if (useAcceleration != null) {
            out.add("use_acceleration=" + useAcceleration);
        }
        if (maximumGradeLevel != null) {
            out.add("maximum_grade_level=" + maximumGradeLevel);
        }
        if (preferredSpeedFactor != null) {
            out.add("preferred_speed_factor=" + preferredSpeedFactor);
        }
        if (problematicSpeedFactor != null) {
            out.add("problematic_speed_factor=" + problematicSpeedFactor);
        }
        if (conditionalAccess != null) {
            out.add("conditional_access=" + conditionalAccess);
        }
        if (conditionalSpeed != null) {
            out.add("conditional_speed=" + conditionalSpeed);
        }
        return String.join("|", out);
    }

    @JsonIgnore
    public void copyProperties(EncoderOptionsProperties source, boolean overwrite) {
        if (source == null) {
            return;
        }

        this.blockFords = (this.blockFords == null || (source.blockFords != null && overwrite)) ? source.blockFords : this.blockFords;
        this.considerElevation = (this.considerElevation == null || (source.considerElevation != null && overwrite)) ? source.considerElevation : this.considerElevation;
        this.turnCosts = (this.turnCosts == null || (source.turnCosts != null && overwrite)) ? source.turnCosts : this.turnCosts;
        this.useAcceleration = (this.useAcceleration == null || (source.useAcceleration != null && overwrite)) ? source.useAcceleration : this.useAcceleration;
        this.maximumGradeLevel = (this.maximumGradeLevel == null || (source.maximumGradeLevel != null && overwrite)) ? source.maximumGradeLevel : this.maximumGradeLevel;
        this.preferredSpeedFactor = (this.preferredSpeedFactor == null || (source.preferredSpeedFactor != null && overwrite)) ? source.preferredSpeedFactor : this.preferredSpeedFactor;
        this.problematicSpeedFactor = (this.problematicSpeedFactor == null || (source.problematicSpeedFactor != null && overwrite)) ? source.problematicSpeedFactor : this.problematicSpeedFactor;
        this.conditionalAccess = (this.conditionalAccess == null || (source.conditionalAccess != null && overwrite)) ? source.conditionalAccess : this.conditionalAccess;
        this.conditionalSpeed = (this.conditionalSpeed == null || (source.conditionalSpeed != null && overwrite)) ? source.conditionalSpeed : this.conditionalSpeed;
    }
}


