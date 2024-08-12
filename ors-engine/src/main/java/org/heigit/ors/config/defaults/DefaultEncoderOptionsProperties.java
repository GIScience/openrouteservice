package org.heigit.ors.config.defaults;


import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.EncoderOptionsProperties;

@EqualsAndHashCode(callSuper = true)
public class DefaultEncoderOptionsProperties extends EncoderOptionsProperties {

    public DefaultEncoderOptionsProperties() {
        this(false, null);
    }

    public DefaultEncoderOptionsProperties(Boolean setGlobalDefaults) {
        this(setGlobalDefaults, null);
    }

    public DefaultEncoderOptionsProperties(EncoderNameEnum encoderName) {
        this(false, encoderName);
    }

    public DefaultEncoderOptionsProperties(Boolean setGlogalDefaults, EncoderNameEnum encoderName) {
        if (setGlogalDefaults) {
            setBlockFords(false);
            setTurnCosts(true);
            setConsiderElevation(false);
            setUseAcceleration(false);
            setConditionalAccess(false); // Default from EncodingManager.java
            setMaximumGradeLevel(null); // TODO find default
            setPreferredSpeedFactor(null); // TODO find default
            setProblematicSpeedFactor(null); // TODO find default
            setConditionalSpeed(false); // TODO find default
        }
        if (encoderName != null) {
            switch (encoderName) {
                case DRIVING_CAR -> {
                    // Just set the ones from below
                    setTurnCosts(true);
                    setBlockFords(false);
                    setUseAcceleration(true);
                    setConsiderElevation(false);
                }
                case DRIVING_HGV -> {
                    setTurnCosts(true);
                    setBlockFords(false);
                    setUseAcceleration(true);
                }
                case CYCLING_REGULAR, CYCLING_MOUNTAIN, CYCLING_ROAD, CYCLING_ELECTRIC -> {
                    setConsiderElevation(true);
                    setTurnCosts(true);
                    setBlockFords(false);
                }
                case FOOT_HIKING, FOOT_WALKING, WHEELCHAIR, PUBLIC_TRANSPORT -> {
                    setTurnCosts(false);
                    setBlockFords(false);
                }
                default -> {
                }
            }
        }

    }
}