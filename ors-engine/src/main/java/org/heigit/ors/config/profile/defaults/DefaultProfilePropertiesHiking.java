package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesHiking extends DefaultProfileProperties {
    public DefaultProfilePropertiesHiking() {
        super();
        this.setEncoderName(EncoderNameEnum.FOOT_HIKING);
    }
}
