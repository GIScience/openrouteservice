package org.heigit.ors.config.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesBikeElectric extends ProfileProperties {
    public DefaultProfilePropertiesBikeElectric() {
        this(false);
    }

    public DefaultProfilePropertiesBikeElectric(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.CYCLING_ELECTRIC);
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
        setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
    }
}
