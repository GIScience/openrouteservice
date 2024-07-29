package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class EncoderOptionsProperties {
    @JsonProperty("block_fords")
    private Boolean blockFords;
    @JsonProperty("consider_elevation")
    private Boolean considerElevation;
    @JsonProperty("turn_costs")
    private Boolean turnCosts;
    @JsonProperty("use_acceleration")
    private Boolean useAcceleration;
    @JsonProperty("maximum_grade_level")
    private Integer maximumGradeLevel;
    @JsonProperty("preferred_speed_factor")
    private Double preferredSpeedFactor;
    @JsonProperty("problematic_speed_factor")
    private Double problematicSpeedFactor;
    @JsonProperty("conditional_access")
    private Boolean conditionalAccess;
    @JsonProperty("conditional_speed")
    private Boolean conditionalSpeed;

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
}