package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesCar extends DefaultProfileProperties {

    public DefaultProfilePropertiesCar() {
        super();
        this.setEncoderName(EncoderNameEnum.DRIVING_CAR);
    }
}
