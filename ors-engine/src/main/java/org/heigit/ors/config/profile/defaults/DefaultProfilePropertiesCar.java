package org.heigit.ors.config.profile.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesCar extends ProfileProperties {

    public DefaultProfilePropertiesCar() {
        this(false);
    }

    public DefaultProfilePropertiesCar(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.DRIVING_CAR);
        if (setDefaults) {
            // Set the default extended storage properties
            DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
            setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
        }
    }
}
