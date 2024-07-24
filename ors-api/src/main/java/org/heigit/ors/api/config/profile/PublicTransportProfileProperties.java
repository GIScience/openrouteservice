package org.heigit.ors.api.config.profile;

public class PublicTransportProfileProperties extends ProfileProperties {
    public PublicTransportProfileProperties() {
        super();
        this.setEncoderName("public-transport");
        getEncoderOptions().setBlockFords(false);
        setElevation(true);
        setMaximumVisitedNodes(1000000);
        setGtfsFile("");
    }
}
