package org.heigit.ors.api.config.profile;

public class PublicTransportProfileProperties extends ProfileProperties {
    public PublicTransportProfileProperties() {
        super();
        this.setEncoderName("public-transport");
        this.setEncoderFlagsSize(999);
//#      public-transport:
//#        profile: public-transport
//#        encoder_options:
//#          block_fords: false
//#        elevation: true
//#        maximum_visited_nodes: 1000000
//#        gtfs_file: ./src/test/files/vrn_gtfs_cut.zip
    }
}
