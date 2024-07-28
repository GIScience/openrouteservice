package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class HgvProfileProperties extends ProfileProperties {
    public HgvProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.DRIVING_HGV);
    }
}
