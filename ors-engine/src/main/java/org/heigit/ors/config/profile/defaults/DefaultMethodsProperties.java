package org.heigit.ors.config.profile.defaults;

import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;

@Setter
@Getter
public class DefaultMethodsProperties {

    private DefaultCHProperties ch = new DefaultCHProperties();
    private DefaultLMProperties lm = new DefaultLMProperties();
    private DefaultCoreProperties core = new DefaultCoreProperties();
    private DefaultFastIsochroneProperties fastisochrones = new DefaultFastIsochroneProperties();

    public DefaultMethodsProperties() {
    }

    public DefaultMethodsProperties(EncoderNameEnum encoderName) {
        this();
        if (encoderName == null) {
            encoderName = EncoderNameEnum.UNKNOWN;
        }

        //TODO
        switch (encoderName) {
            case DRIVING_CAR -> {
            }
            case DRIVING_HGV -> {
            }
            case CYCLING_REGULAR -> {
            }
            case FOOT_WALKING, WHEELCHAIR, PUBLIC_TRANSPORT -> {
            }
            default -> {
            }
        }
    }

    private static class DefaultCHProperties {

    }

    private static class DefaultLMProperties {

    }

    private static class DefaultCoreProperties {

    }

    private static class DefaultFastIsochroneProperties {
    }

}
