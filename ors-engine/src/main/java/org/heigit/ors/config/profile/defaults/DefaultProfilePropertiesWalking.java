package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesWalking extends DefaultProfileProperties {
    public DefaultProfilePropertiesWalking() {
        super();
        this.setEncoderName(EncoderNameEnum.FOOT_WALKING);
    }
}
