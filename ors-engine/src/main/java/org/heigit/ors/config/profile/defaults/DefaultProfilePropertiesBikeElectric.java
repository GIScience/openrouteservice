package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesBikeElectric extends ProfileProperties {
    public DefaultProfilePropertiesBikeElectric() {
        this(false);
    }

    public DefaultProfilePropertiesBikeElectric(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.CYCLING_ELECTRIC);
    }
}
