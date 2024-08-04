package org.heigit.ors.config.defaults;

import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesBikeRoad extends ProfileProperties {
    public DefaultProfilePropertiesBikeRoad() {
        this(false);
    }

    public DefaultProfilePropertiesBikeRoad(Boolean setDefaults) {
        super(setDefaults, EncoderNameEnum.CYCLING_ROAD);
        DefaultExtendedStoragesProperties defaultExtendedStoragesProperties = new DefaultExtendedStoragesProperties(this.getEncoderName());
        setExtStorages(defaultExtendedStoragesProperties.getExtStorages());
    }
}
