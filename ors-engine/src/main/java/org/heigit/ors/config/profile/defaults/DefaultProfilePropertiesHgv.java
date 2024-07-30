package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesHgv extends DefaultProfileProperties {
    public DefaultProfilePropertiesHgv() {
        super();
        this.setEncoderName(EncoderNameEnum.DRIVING_HGV);
    }
}
