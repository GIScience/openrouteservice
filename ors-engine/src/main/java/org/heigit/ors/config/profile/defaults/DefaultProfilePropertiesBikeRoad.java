package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesBikeRoad extends ProfileProperties {
    public DefaultProfilePropertiesBikeRoad() {
        this(false);
    }

    public DefaultProfilePropertiesBikeRoad(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.CYCLING_ROAD);
    }
}
