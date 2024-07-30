package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesWheelchair extends ProfileProperties {
    public DefaultProfilePropertiesWheelchair() {
        this(false);
    }

    public DefaultProfilePropertiesWheelchair(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.WHEELCHAIR);

        if (setDefaults) setMaximumSnappingRadius(50);

    }
}
