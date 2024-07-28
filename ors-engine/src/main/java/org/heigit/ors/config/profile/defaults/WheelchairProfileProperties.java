package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class WheelchairProfileProperties extends ProfileProperties {
    public WheelchairProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.WHEELCHAIR);
        setMaximumSnappingRadius(50);
    }
}
