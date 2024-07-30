package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesHgv extends ProfileProperties {
    public DefaultProfilePropertiesHgv() {
        this(false);
    }

    public DefaultProfilePropertiesHgv(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.DRIVING_HGV);
    }
}
