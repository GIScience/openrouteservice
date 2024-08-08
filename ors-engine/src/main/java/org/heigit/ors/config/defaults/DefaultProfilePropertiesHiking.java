package org.heigit.ors.config.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesHiking extends ProfileProperties {
    public DefaultProfilePropertiesHiking() {
        this(false);
    }

    public DefaultProfilePropertiesHiking(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.FOOT_HIKING);
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
        setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
    }
}
