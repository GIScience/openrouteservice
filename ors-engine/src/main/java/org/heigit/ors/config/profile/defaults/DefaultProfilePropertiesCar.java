package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

public class DefaultProfilePropertiesCar extends ProfileProperties {

    public DefaultProfilePropertiesCar() {
        this(false);
    }

    public DefaultProfilePropertiesCar(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        if (setDefaults) {
            // Set the default extended storage properties
            DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
            setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
        }
    }
}
