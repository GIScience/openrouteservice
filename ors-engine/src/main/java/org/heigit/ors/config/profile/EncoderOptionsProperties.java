package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(NON_NULL)
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

    @JsonIgnore
    public static EncoderOptionsProperties getEncoderOptionsProperties(EncoderNameEnum encoderName) {
        EncoderOptionsProperties encoderOptions = new EncoderOptionsProperties();
        switch (encoderName) {
            case DRIVING_CAR, DRIVING_HGV -> {
                encoderOptions.setTurnCosts(true);
                encoderOptions.setBlockFords(false);
                encoderOptions.setUseAcceleration(true);
            }
            case CYCLING_REGULAR, CYCLING_MOUNTAIN, CYCLING_ROAD, CYCLING_ELECTRIC -> {
                encoderOptions.setConsiderElevation(true);
                encoderOptions.setTurnCosts(true);
                encoderOptions.setBlockFords(false);
            }
            case FOOT_HIKING, FOOT_WALKING, PUBLIC_TRANSPORT -> {
                encoderOptions.setBlockFords(false);
            }
            case WHEELCHAIR -> {
                encoderOptions.setBlockFords(true);
            }
            default -> {
            }
        }
        return encoderOptions;

//    TODO: check and apply the changes Julian was going to make to the default values
//    public DefaultEncoderOptionsProperties(Boolean setGlogalDefaults, EncoderNameEnum encoderName) {
//            if (setGlogalDefaults) {
//                setBlockFords(false);
//                setTurnCosts(true);
//                setConsiderElevation(false);
//                setUseAcceleration(false);
//                setConditionalAccess(false); // Default from EncodingManager.java
//                setMaximumGradeLevel(null); // TODO find default
//                setPreferredSpeedFactor(null); // TODO find default
//                setProblematicSpeedFactor(null); // TODO find default
//                setConditionalSpeed(false); // TODO find default
//            }
//            if (encoderName != null) {
//                switch (encoderName) {
//                    case DRIVING_CAR -> {
//                        // Just set the ones from below
//                        setTurnCosts(true);
//                        setBlockFords(false);
//                        setUseAcceleration(true);
//                        setConsiderElevation(false);
//                    }
//                    case DRIVING_HGV -> {
//                        setTurnCosts(true);
//                        setBlockFords(false);
//                        setUseAcceleration(true);
//                    }
//                    case CYCLING_REGULAR, CYCLING_MOUNTAIN, CYCLING_ROAD, CYCLING_ELECTRIC -> {
//                        setConsiderElevation(true);
//                        setTurnCosts(true);
//                        setBlockFords(false);
//                    }
//                    case FOOT_HIKING, FOOT_WALKING, WHEELCHAIR, PUBLIC_TRANSPORT -> {
//                        setTurnCosts(false);
//                        setBlockFords(false);
//                    }
//                    default -> {
//                    }
//                }
//            }
//
//        }
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

    public void merge(EncoderOptionsProperties other) {
        blockFords = blockFords == null ? other.blockFords : blockFords;
        considerElevation = considerElevation == null ? other.considerElevation : considerElevation;
        turnCosts = turnCosts == null ? other.turnCosts : turnCosts;
        useAcceleration = useAcceleration == null ? other.useAcceleration : useAcceleration;
        maximumGradeLevel = maximumGradeLevel == null ? other.maximumGradeLevel : maximumGradeLevel;
        preferredSpeedFactor = preferredSpeedFactor == null ? other.preferredSpeedFactor : preferredSpeedFactor;
        problematicSpeedFactor = problematicSpeedFactor == null ? other.problematicSpeedFactor : problematicSpeedFactor;
        conditionalAccess = conditionalAccess == null ? other.conditionalAccess : conditionalAccess;
        conditionalSpeed = conditionalSpeed == null ? other.conditionalSpeed : conditionalSpeed;
    }
}


