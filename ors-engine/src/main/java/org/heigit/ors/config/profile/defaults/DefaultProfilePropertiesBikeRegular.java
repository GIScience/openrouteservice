package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesBikeRegular extends ProfileProperties {
    public DefaultProfilePropertiesBikeRegular() {
        this(false);
    }

    public DefaultProfilePropertiesBikeRegular(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.CYCLING_REGULAR);
    }
}
