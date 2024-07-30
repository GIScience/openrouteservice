package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesBikeElectric extends DefaultProfileProperties {
    public DefaultProfilePropertiesBikeElectric() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_ELECTRIC);
    }
}
