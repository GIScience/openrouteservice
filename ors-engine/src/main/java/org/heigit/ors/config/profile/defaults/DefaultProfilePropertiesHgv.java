package org.heigit.ors.config.profile.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesHgv extends ProfileProperties {
    public DefaultProfilePropertiesHgv() {
        this(false);
    }

    public DefaultProfilePropertiesHgv(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.DRIVING_HGV);
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
        setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
    }
}
