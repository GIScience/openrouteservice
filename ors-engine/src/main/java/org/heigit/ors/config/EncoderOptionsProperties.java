package org.heigit.ors.config;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

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

    public Boolean getBlockFords() {
        return blockFords;
    }

    public void setBlockFords(Boolean blockFords) {
        this.blockFords = blockFords;
    }

    public Boolean getConsiderElevation() {
        return considerElevation;
    }

    public void setConsiderElevation(Boolean considerElevation) {
        this.considerElevation = considerElevation;
    }

    public Boolean getTurnCosts() {
        return turnCosts;
    }

    public void setTurnCosts(Boolean turnCosts) {
        this.turnCosts = turnCosts;
    }

    public Boolean getUseAcceleration() {
        return useAcceleration;
    }

    public void setUseAcceleration(Boolean useAcceleration) {
        this.useAcceleration = useAcceleration;
    }

    public Integer getMaximumGradeLevel() {
        return maximumGradeLevel;
    }

    public void setMaximumGradeLevel(Integer maximumGradeLevel) {
        this.maximumGradeLevel = maximumGradeLevel;
    }

    public Double getPreferredSpeedFactor() {
        return preferredSpeedFactor;
    }

    public void setPreferredSpeedFactor(Double preferredSpeedFactor) {
        this.preferredSpeedFactor = preferredSpeedFactor;
    }

    public Double getProblematicSpeedFactor() {
        return problematicSpeedFactor;
    }

    public void setProblematicSpeedFactor(Double problematicSpeedFactor) {
        this.problematicSpeedFactor = problematicSpeedFactor;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Field entry : this.getClass().getDeclaredFields()) {
            try {
                Object value = entry.get(this);
                if (value != null) {
                    if (!output.isEmpty()) {
                        output.append("|");
                    }
                    output.append(value);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return output.toString();

    }
}