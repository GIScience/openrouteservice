package org.heigit.ors.config.profile.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesBikeRegular extends ProfileProperties {
    public DefaultProfilePropertiesBikeRegular() {
        this(false);
    }

    public DefaultProfilePropertiesBikeRegular(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.CYCLING_REGULAR);
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
        setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
    }
}
