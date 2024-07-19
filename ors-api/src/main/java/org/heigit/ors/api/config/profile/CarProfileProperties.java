package org.heigit.ors.api.config.profile;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("driving-car")
public class CarProfileProperties extends ProfileProperties {
    public CarProfileProperties() {
        super();
        this.setEncoderFlagsSize(999);
    }
}
