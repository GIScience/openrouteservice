package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class BikeRoadProfileProperties extends ProfileProperties {
    public BikeRoadProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.CYCLING_ROAD);
    }
}
