package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesBikeMountain extends ProfileProperties {
    public DefaultProfilePropertiesBikeMountain() {
        this(false);
    }

    public DefaultProfilePropertiesBikeMountain(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.CYCLING_MOUNTAIN);
    }
}
