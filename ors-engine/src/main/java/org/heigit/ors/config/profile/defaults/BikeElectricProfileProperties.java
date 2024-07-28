package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class BikeElectricProfileProperties extends ProfileProperties {
    public BikeElectricProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_ELECTRIC);
    }
}
