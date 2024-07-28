package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class BikeRegularProfileProperties extends ProfileProperties {
    public BikeRegularProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_REGULAR);
    }
}
