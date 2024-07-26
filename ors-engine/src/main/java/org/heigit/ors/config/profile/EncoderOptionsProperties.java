package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class EncoderOptionsProperties {
    @JsonIgnore
    Logger logger = LoggerFactory.getLogger(EncoderOptionsProperties.class);
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
}


