package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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
}