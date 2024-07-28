package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesCar extends ProfileProperties {

    public DefaultProfilePropertiesCar() {
        super();
        this.setEncoderName(EncoderNameEnum.DRIVING_CAR);
    }
}
