package org.heigit.ors.config.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesWheelchair extends ProfileProperties {
    public DefaultProfilePropertiesWheelchair() {
        this(false);
    }

    public DefaultProfilePropertiesWheelchair(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.WHEELCHAIR);
        if (setDefaults) {
            setMaximumSnappingRadius(50);
            DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
            setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
        }

    }
}
