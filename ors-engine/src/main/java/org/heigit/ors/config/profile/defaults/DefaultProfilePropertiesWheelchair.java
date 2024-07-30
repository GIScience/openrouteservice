package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;

public class DefaultProfilePropertiesWheelchair extends DefaultProfileProperties {
    public DefaultProfilePropertiesWheelchair() {
        super();
        this.setEncoderName(EncoderNameEnum.WHEELCHAIR);
        setMaximumSnappingRadius(50);
    }
}
