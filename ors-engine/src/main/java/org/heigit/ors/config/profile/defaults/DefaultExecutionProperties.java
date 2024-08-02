package org.heigit.ors.config.profile.defaults;

import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ExecutionProperties;

@Getter
@Setter
public class DefaultExecutionProperties extends ExecutionProperties {
    public DefaultExecutionProperties() {
        super();
        setMethods(new MethodsProperties(true));
        getMethods().getLm().setActiveLandmarks(8);
        getMethods().getCore().setActiveLandmarks(6);
    }

    public DefaultExecutionProperties(EncoderNameEnum encoderName) {
        this();
        if (encoderName == null) {
            encoderName = EncoderNameEnum.UNKNOWN;
        }

        switch (encoderName) {
            case DRIVING_CAR -> {
                getMethods().getLm().setActiveLandmarks(6);
                getMethods().getCore().setActiveLandmarks(6);
            }
            case DRIVING_HGV -> getMethods().getCore().setActiveLandmarks(6);
            default -> {
            }
        }
    }
}
