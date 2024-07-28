package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class BikeMountainProfileProperties extends ProfileProperties {
    public BikeMountainProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_MOUNTAIN);
    }
}
