package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesWalking extends ProfileProperties {
    public DefaultProfilePropertiesWalking() {
        this(false);
    }

    public DefaultProfilePropertiesWalking(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.FOOT_WALKING);
    }
}
