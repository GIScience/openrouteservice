package org.heigit.ors.config.profile.defaults;

import org.heigit.ors.config.profile.ProfileProperties;

import java.nio.file.Path;

public class PublicTransportProfileProperties extends ProfileProperties {
    public PublicTransportProfileProperties() {
        super();
        this.setEncoderName("public-transport");
        getEncoderOptions().setBlockFords(false);
        setElevation(true);
        setMaximumVisitedNodes(1000000);
        setGtfsFile(Path.of(""));
    }
}
