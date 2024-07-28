package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class HikingProfileProperties extends ProfileProperties {
    public HikingProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.FOOT_HIKING);
    }
}
