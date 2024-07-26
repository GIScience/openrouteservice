package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;

import java.nio.file.Path;

public class PublicTransportProfileProperties extends ProfileProperties {
    public PublicTransportProfileProperties() {
        super();
        this.setEncoderName(EncoderNameEnum.PUBLIC_TRANSPORT);
        getEncoderOptions().setBlockFords(false);
        setElevation(true);
        setMaximumVisitedNodes(1000000);
        setGtfsFile(Path.of(""));
    }
}
