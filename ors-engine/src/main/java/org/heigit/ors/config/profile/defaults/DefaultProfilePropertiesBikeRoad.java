package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesBikeRoad extends DefaultProfileProperties {
    public DefaultProfilePropertiesBikeRoad() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_ROAD);
    }
}
