package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class WalkingProfileProperties extends ProfileProperties {
    public WalkingProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.FOOT_WALKING);
    }
}
