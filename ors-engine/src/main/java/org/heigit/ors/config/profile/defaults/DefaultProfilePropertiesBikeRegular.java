package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesBikeRegular extends ProfileProperties {
    public DefaultProfilePropertiesBikeRegular() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_REGULAR);
    }
}
