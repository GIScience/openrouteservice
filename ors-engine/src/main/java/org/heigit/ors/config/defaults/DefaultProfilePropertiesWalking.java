package org.heigit.ors.config.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesWalking extends ProfileProperties {
    public DefaultProfilePropertiesWalking() {
        this(false);
    }

    public DefaultProfilePropertiesWalking(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.FOOT_WALKING);
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
        setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
    }
}