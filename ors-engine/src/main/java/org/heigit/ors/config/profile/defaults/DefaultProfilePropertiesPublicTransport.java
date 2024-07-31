package org.heigit.ors.config.profile.defaults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

import java.nio.file.Path;

@JsonIgnoreProperties({"ext_storages"})
@EqualsAndHashCode(callSuper = false)
public class DefaultProfilePropertiesPublicTransport extends ProfileProperties {
    public DefaultProfilePropertiesPublicTransport() {
        this(false);
    }

    public DefaultProfilePropertiesPublicTransport(Boolean setDefaults) {
        super(setDefaults);
        this.setEncoderName(EncoderNameEnum.PUBLIC_TRANSPORT);

        if (setDefaults) {
            this.setEncoderName(EncoderNameEnum.PUBLIC_TRANSPORT);
            setElevation(true);
            setMaximumVisitedNodes(1000000);
            setGtfsFile(Path.of(""));
        }
    }
}
